package tech.tykn.tyknid;

import android.system.ErrnoException;
import android.system.Os;
import android.text.InputFilter;
import android.util.Log;

import org.hyperledger.indy.sdk.ErrorCode;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Service {

    private String TAG = "TyknID::Service >>>> ";
    private Wallet __wallet = null;
    private Boolean isWalletOpen = false;
    private Pool __pool = null;
    private final String MASTER_SECRET_KEY = "masterSecret";
    private final String POOL_CONFIG_NAME = "pool_config_name";
    final String RUNTIME_CONFIG = "{\"collect_backtrace\": true}";

    private void libindyInit(){

        System.loadLibrary("indy");
        if(!LibIndy.isInitialized()){
            LibIndy.init();
            LibIndy.setRuntimeConfig(RUNTIME_CONFIG);
        }

    }
    private Pool openPool(String poolConfig) throws IndyException, ExecutionException, InterruptedException {
        if (__pool != null) {
            __pool = Pool.openPoolLedger(POOL_CONFIG_NAME, poolConfig).get();
        }
        return __pool;

    }

    private void closeWallet() throws InterruptedException, ExecutionException, IndyException {
        if(__wallet!=null && isWalletOpen){
            __wallet.close();
            isWalletOpen = false;
        }
    }

    private Wallet openWallet(Map<String, String> walletConfig) throws IndyException {
        if ( !isWalletOpen) {
            try {
                __wallet = Wallet.openWallet(walletConfig.get("config"),walletConfig.get("creds")).get();
                isWalletOpen = true;
                Log.e(TAG, "openWallet: opened wallet");
                return __wallet;

            } catch (IndyException e) {
                e.printStackTrace();
                Log.e(TAG, "openWallet: unable to open wallet", e);
                return null;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        return __wallet;
    }


    private Map<String, String> getWalletConfig(String walletName, String walletKey) {
        final String WALLET_CONFIG = "{ \"id\":\"" + walletName + "\"}";
        final String WALLET_CREDENTIALS = "{\"key\":\"" + walletKey + "\"}";
        Map<String, String> config = new HashMap<>();
        config.put("config", WALLET_CONFIG);
        config.put("creds", WALLET_CREDENTIALS);
        return config;
    }
    public void deleteWallet(String walletName, String walletKey) throws IndyException, ExecutionException, InterruptedException {
        closeWallet();
        Wallet.deleteWallet(getWalletConfig(walletName,walletKey).get("config"),getWalletConfig(walletName,walletKey).get("creds")).get();
    }

    public void createWallet(String walletName, String walletKey) throws WalletCreationException, WalletAlreadyExistException, IndyException, ExecutionException, InterruptedException {
        libindyInit();
        Wallet wallet = null;

        Log.d(TAG, "createWallet: creating wallet with name: " + walletName);


        try {
            Wallet.createWallet(getWalletConfig(walletName,walletKey).get("config"),getWalletConfig(walletName,walletKey).get("creds")).get();
        } catch (IndyException e) {
            e.printStackTrace();
            Log.e(TAG, "createWallet: unable to create wallet", e);
            if (e.getSdkErrorCode() == ErrorCode.WalletAlreadyExistsError.value()) {
                throw new WalletAlreadyExistException();
            } else {
                throw new WalletCreationException();
            }
        }


        wallet = openWallet(getWalletConfig(walletName, walletKey));
        if (wallet != null) {
            String masterSecret = Anoncreds.proverCreateMasterSecret(wallet, MASTER_SECRET_KEY).get();
            Log.d(TAG, "createWallet: mastersecret created >> " + masterSecret);
            closeWallet();
        } else {
            Log.d(TAG, "PANIC: createWallet: wallet is not open");
        }

        Log.d(TAG, "createWallet: wallet created" + walletName);
    }

    public DID generatePADID(String walletName, String walletKey) throws IndyException, ExecutionException, InterruptedException {
        Wallet wallet = null;
        wallet = openWallet(getWalletConfig(walletName, walletKey));
        DidResults.CreateAndStoreMyDidResult __did = Did.createAndStoreMyDid(wallet, "{}").get();
        DID _did = new DID();
        _did.setDID(__did.getDid());
        _did.setVerkey(__did.getVerkey());
        Log.d(TAG, "createDID: DID created :>> " + _did.getDID() + " verkey :>> "+ _did.getVerkey());
        closeWallet();

        return _did;
    }

    public ProverCreateCredentialRequestResult createCredentialRequest(String walletName, String walletKey, String poolConfig, String DID, String credDefId, String credDefOfferJson) throws IndyException, ExecutionException, InterruptedException {

        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        Pool pool = openPool(poolConfig);
        String credDefRequestJson = Ledger.buildCredDefRequest(DID, credDefId).get();
        String credDefResponseJson = Ledger.submitRequest(pool, credDefRequestJson).get();
        pool.close();
        LedgerResults.ParseResponseResult resp = Ledger.parseGetCredDefResponse(credDefResponseJson).get();

        resp.getObjectJson();

        ProverCreateCredentialRequestResult result =
                Anoncreds.proverCreateCredentialReq(wallet, DID, credDefOfferJson, resp.getObjectJson(), MASTER_SECRET_KEY).get();
       closeWallet();
        return result;
    }

    public void saveCredential(String walletName, String walletKey, String poolConfig, String credDefId, String credDefRequestJson, String credJson) throws IndyException, ExecutionException, InterruptedException {

        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        Pool pool = openPool(poolConfig);
        String _credDefRequestJson = Ledger.buildCredDefRequest("", credDefId).get();
        String credDefResponseJson = Ledger.submitRequest(pool, credDefRequestJson).get();
        pool.close();
        LedgerResults.ParseResponseResult resp = Ledger.parseGetCredDefResponse(credDefResponseJson).get();

        Anoncreds.proverStoreCredential(wallet, credDefId, credDefRequestJson, resp.getObjectJson(), credJson, "");


    }

    private boolean searchValueInJSONArray(String value, JSONArray array) throws JSONException {
        boolean found = false;
        for (int i = 0; i < array.length(); i++)
            if (array.getString(i).equals(value))
                found = true;
        return found;
    }

    public void createProof(String walletName, String walletKey, String poolConfig, String credDefId, String proofRequest) throws IndyException, JSONException, ExecutionException, InterruptedException {

        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        Pool pool = openPool(poolConfig);

        String credentials_for_proof_request_json_string = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
        JSONObject credentialsForProofRequest = new JSONObject(credentials_for_proof_request_json_string);
        JSONObject proofRequestJSON = new JSONObject(proofRequest);

        JSONArray attributesJSON = credentialsForProofRequest.getJSONArray("attrs");
        JSONArray predicatesJSON = credentialsForProofRequest.getJSONArray("predicates");

        JSONArray requestedAttributesJSON = proofRequestJSON.getJSONArray("requested_attributes");
        JSONArray requestedPredicatesJSON = proofRequestJSON.getJSONArray("requested_predicates");

        for (int attrIndex = 0; attrIndex < requestedAttributesJSON.length(); attrIndex++) {
            String attribute = requestedAttributesJSON.getString(attrIndex);
            if (searchValueInJSONArray(attribute, attributesJSON)) {
//                JSONObject credentialOptions = attributesJSON.getJSONObject();
            }
        }

        String requestCredentialsJsonString = "";
        String schemasJsonString = "";
        String credDefsJsonString = "";
        Anoncreds.proverCreateProof(wallet, proofRequest, requestCredentialsJsonString, MASTER_SECRET_KEY, schemasJsonString, credDefsJsonString, "{}");

    }
}
