package tech.tykn.tyknid;

import android.util.Log;

import org.hyperledger.indy.sdk.ErrorCode;
import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.LibIndy;
import org.hyperledger.indy.sdk.anoncreds.Anoncreds;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults.ProverCreateCredentialRequestResult;
import org.hyperledger.indy.sdk.did.Did;
import org.hyperledger.indy.sdk.did.DidResults;
import org.hyperledger.indy.sdk.ledger.Ledger;
import org.hyperledger.indy.sdk.ledger.LedgerResults;
import org.hyperledger.indy.sdk.pool.Pool;
import org.hyperledger.indy.sdk.pool.PoolJSONParameters;
import org.hyperledger.indy.sdk.pool.PoolLedgerConfigExistsException;
import org.hyperledger.indy.sdk.wallet.Wallet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Service {

    private String TAG = "TyknID::Service >>>> ";
    private Wallet __wallet = null;
    private Boolean isWalletOpen = false;
    private Pool __pool = null;
    private final String MASTER_SECRET_KEY = "masterSecret";
    private final String DEFAULT_POOL_NAME = "default_pool";
    final String RUNTIME_CONFIG = "{\"collect_backtrace\": true}";
    public static final int PROTOCOL_VERSION = 2;

    private void libindyInit() {

        System.loadLibrary("indy");
        if (!LibIndy.isInitialized()) {
            LibIndy.init();
            LibIndy.setRuntimeConfig(RUNTIME_CONFIG);
        }

    }

    private String createPoolLedgerConfig(File genesisTxnFile) {
        Log.d(TAG, "createPoolLedgerConfig() called with: genesisTxnFile = [" + genesisTxnFile + "]");
        PoolJSONParameters.CreatePoolLedgerConfigJSONParameter createPoolLedgerConfigJSONParameter
                = new PoolJSONParameters.CreatePoolLedgerConfigJSONParameter(genesisTxnFile.getAbsolutePath());
        try {
            Pool.createPoolLedgerConfig(DEFAULT_POOL_NAME, createPoolLedgerConfigJSONParameter.toJson()).get();
        } catch (java.util.concurrent.ExecutionException | PoolLedgerConfigExistsException e) {
            Log.e(TAG, "createPoolLedgerConfig: unable to create pool config due to ::", e);
        } catch (InterruptedException | IndyException e) {
            e.printStackTrace();
        }

        return DEFAULT_POOL_NAME;
    }

    private Pool openPool(File poolConfig) throws IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "openPool() called with: poolConfig = [" + poolConfig + "]");
        Pool.setProtocolVersion(PROTOCOL_VERSION);
        try {
            createPoolLedgerConfig(poolConfig);
        } catch (Throwable e) {
            Log.e(TAG, "createPoolLedgerConfig: unable to create pool config due to ::", e);
        }

        PoolJSONParameters.OpenPoolLedgerJSONParameter config = new PoolJSONParameters.OpenPoolLedgerJSONParameter(null, null);
        if (__pool == null) {
            __pool = Pool.openPoolLedger(DEFAULT_POOL_NAME, config.toJson()).get();
        }
        return __pool;

    }

    private void closePool() throws InterruptedException, ExecutionException, IndyException {
        Log.d(TAG, "closePool() called");
        if (__pool != null) {
            __pool.close();
            __pool = null;
        }
    }

    private void closeWallet() throws InterruptedException, ExecutionException, IndyException {
        Log.d(TAG, "closeWallet() called");
        if (__wallet != null && isWalletOpen) {
            __wallet.close();
            isWalletOpen = false;
        }
    }

    private Wallet openWallet(Map<String, String> walletConfig) {
        Log.d(TAG, "openWallet() called with: walletConfig = [" + walletConfig + "]");
        if (!isWalletOpen) {
            try {
                libindyInit();
                __wallet = Wallet.openWallet(walletConfig.get("config"), walletConfig.get("creds")).get();
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
        Log.d(TAG, "getWalletConfig() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "]");
        final String WALLET_CONFIG = "{ \"id\":\"" + walletName + "\"}";
        final String WALLET_CREDENTIALS = "{\"key\":\"" + walletKey + "\"}";
        Map<String, String> config = new HashMap<>();
        config.put("config", WALLET_CONFIG);
        config.put("creds", WALLET_CREDENTIALS);
        return config;
    }

    public void deleteWallet(String walletName, String walletKey) throws IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "deleteWallet() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "]");
        closeWallet();
        Wallet.deleteWallet(getWalletConfig(walletName, walletKey).get("config"), getWalletConfig(walletName, walletKey).get("creds")).get();
    }

    public void createWallet(String walletName, String walletKey) throws WalletCreationException, WalletAlreadyExistException, IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "createWallet() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "]");
        libindyInit();
        Wallet wallet = null;

        Log.d(TAG, "createWallet: creating wallet with name: " + walletName);


        try {
            Wallet.createWallet(getWalletConfig(walletName, walletKey).get("config"), getWalletConfig(walletName, walletKey).get("creds")).get();
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
            closeWallet();
        } else {
            Log.d(TAG, "PANIC: createWallet: wallet is not open");
        }

        Log.d(TAG, "createWallet: wallet created" + walletName);
    }

    public DID generatePADID(String walletName, String walletKey) throws IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "generatePADID() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "]");
        Wallet wallet = null;
        wallet = openWallet(getWalletConfig(walletName, walletKey));
        DidResults.CreateAndStoreMyDidResult __did = Did.createAndStoreMyDid(wallet, "{}").get();
        DID _did = new DID();
        _did.setDID(__did.getDid());
        _did.setVerkey(__did.getVerkey());
        Log.d(TAG, "createDID: DID created :>> " + _did.getDID() + " verkey :>> " + _did.getVerkey());
        closeWallet();

        return _did;
    }

    private String getCredentialDefinationFromLedger(File poolConfig, String DID, String credDefId) throws InterruptedException, ExecutionException, IndyException {
        Log.d(TAG, "getCredentialDefinationFromLedger() called with: poolConfig = [" + poolConfig + "], DID = [" + DID + "], credDefId = [" + credDefId + "]");
        Pool pool = openPool(poolConfig);
        String credDefRequestJson = Ledger.buildGetCredDefRequest(DID, credDefId).get();
        String credDefResponseJson = Ledger.submitRequest(pool, credDefRequestJson).get();
        closePool();
        LedgerResults.ParseResponseResult resp = Ledger.parseGetCredDefResponse(credDefResponseJson).get();
        return resp.getObjectJson();

    }

    public ProverCreateCredentialRequestResult createCredentialRequest(String walletName, String walletKey, File poolConfig, String DID, String credDefId, String credDefOfferJson) throws IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "createCredentialRequest() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "], poolConfig = [" + poolConfig + "], DID = [" + DID + "], credDefId = [" + credDefId + "], credDefOfferJson = [" + credDefOfferJson + "]");
        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        String CredDefResponse = getCredentialDefinationFromLedger(poolConfig, DID, credDefId);
        ProverCreateCredentialRequestResult result =
                Anoncreds.proverCreateCredentialReq(wallet, DID, credDefOfferJson, CredDefResponse, MASTER_SECRET_KEY).get();
        closeWallet();
        return result;
    }

    public void saveCredential(String walletName, String walletKey, File poolConfig, String DID, String credDefId, String credReqMetadataJson, String credJson) throws IndyException, ExecutionException, InterruptedException {
        Log.d(TAG, "saveCredential() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "], poolConfig = [" + poolConfig + "], DID = [" + DID + "], credDefId = [" + credDefId + "], credReqMetadataJson = [" + credReqMetadataJson + "], credJson = [" + credJson + "]");

        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        String CredDefResponse = getCredentialDefinationFromLedger(poolConfig, DID, credDefId);

        Log.d(TAG, "credential response from ledger: " + CredDefResponse);
        String result = Anoncreds.proverStoreCredential(
                wallet,
                "",
                credReqMetadataJson,
                credJson,
                CredDefResponse, null).get();
        Log.d(TAG, "saveCredential: " + result);
        closeWallet();


    }

    public ProofResult createProof(String walletName, String walletKey, File poolConfig, String credDefId, String proofRequest) throws IndyException, JSONException, ExecutionException, InterruptedException {

        Log.d(TAG, "createProof() called with: walletName = [" + walletName + "], walletKey = [" + walletKey + "], poolConfig = [" + poolConfig + "], credDefId = [" + credDefId + "], proofRequest = [" + proofRequest + "]");
        Wallet wallet = openWallet(getWalletConfig(walletName, walletKey));
        Pool pool = openPool(poolConfig);

        String credentials_for_proof_request_json_string = Anoncreds.proverGetCredentialsForProofReq(wallet, proofRequest).get();
        JSONObject credentialsForProofRequest = new JSONObject(credentials_for_proof_request_json_string);
        JSONObject proofRequestJSON = new JSONObject(proofRequest);

        Log.i(TAG, "credentials from wallet for Proof Request: ");
        Log.i(TAG, credentialsForProofRequest.toString());
        JSONObject attributesJSON = credentialsForProofRequest.getJSONObject("attrs");
        JSONObject predicatesJSON = credentialsForProofRequest.getJSONObject("predicates");

        JSONObject requestedAttributesJSON = proofRequestJSON.getJSONObject("requested_attributes");
        JSONObject requestedPredicatesJSON = proofRequestJSON.getJSONObject("requested_predicates");

        ArrayList schemaIds = new ArrayList<String>();
        ArrayList credentialDefIds = new ArrayList<String>();
        JSONObject credentials = new JSONObject();
        JSONObject schemas = new JSONObject();
        JSONObject credDefs = new JSONObject();
        JSONObject credentialAttributeReferants = new JSONObject();

        if (requestedAttributesJSON != null) {
            for (int keyIndex = 0; keyIndex < requestedAttributesJSON.names().length(); keyIndex++) {
                String attr_referent = requestedAttributesJSON.names().get(keyIndex).toString();
                JSONArray attrsByReferrant = attributesJSON.getJSONArray(attr_referent);
                if (attrsByReferrant.length() > 0) {
                    for (int index = 0; index < attrsByReferrant.length(); index++) {
                        JSONObject credOption = attrsByReferrant.getJSONObject(index);
                        if (credOption != null) {
                            JSONObject cred = credOption.getJSONObject("cred_info");
                            String referant = cred.getString("referent");
                            String schemaId = cred.getString("schema_id");
                            schemaIds.add(schemaId);
                            String credentialDefId = cred.getString("cred_def_id");
                            credentialDefIds.add(credentialDefId);
                            JSONObject credentialAttributeReferant = new JSONObject();
                            credentialAttributeReferant.put("cred_id", referant);
                            credentialAttributeReferant.put("revealed", true);
                            credentialAttributeReferants.put(attr_referent, credentialAttributeReferant);
                        }
                    }
                }
            }
        }

        JSONObject credentialPredicateReferants = new JSONObject();

        if (requestedPredicatesJSON.names() != null) {
            for (int keyIndex = 0; keyIndex < requestedPredicatesJSON.names().length(); keyIndex++) {
                String pred_referent = requestedPredicatesJSON.names().get(keyIndex).toString();
                JSONArray predsByReferrant = predicatesJSON.getJSONArray(pred_referent);
                if (predsByReferrant.length() > 0) {
                    for (int index = 0; index < predsByReferrant.length(); index++) {
                        JSONObject credOption = predsByReferrant.getJSONObject(index);
                        if (credOption != null) {
                            JSONObject cred = credOption.getJSONObject("cred_info");
                            String referant = cred.getString("referent");
                            String schemaId = cred.getString("schema_id");
                            schemaIds.add(schemaId);
                            String credentialDefId = cred.getString("cred_def_id");
                            credentialDefIds.add(credentialDefId);
                            JSONObject credentialPredeicateReferant = new JSONObject();
                            credentialPredeicateReferant.put("cred_id", referant);
                            credentialPredicateReferants.put(pred_referent, credentialPredeicateReferant);
                        }
                    }
                }
            }
        }

        credentials.put("self_attested_attributes", new JSONObject());
        credentials.put("requested_attributes", credentialAttributeReferants);
        credentials.put("requested_predicates", credentialPredicateReferants);

        for (int schemaIndex = 0; schemaIndex < schemaIds.size(); schemaIndex++) {
            String schemaId = schemaIds.get(schemaIndex).toString();
            try {
                String schemaRequestJson = Ledger.buildGetSchemaRequest("GyQyAmN1uyMG4TYS9MiyPW", schemaId).get();
                String schemaResponseJson = Ledger.submitRequest(pool, schemaRequestJson).get();
                LedgerResults.ParseResponseResult result = Ledger.parseGetSchemaResponse(schemaResponseJson).get();
                schemas.put(schemaId, new JSONObject(result.getObjectJson()));
            } catch (IndyException e) {
                Log.e(TAG, "createProof: unable to get schemas", e);
            }

        }

        for (int credDefIndex = 0; credDefIndex < credentialDefIds.size(); credDefIndex++) {
            String credDefinationId = credentialDefIds.get(credDefIndex).toString();
            try {
                String credDefRequestJson = Ledger.buildGetCredDefRequest(null, credDefinationId).get();
                String credDefResponseJson = Ledger.submitRequest(pool, credDefRequestJson).get();
                LedgerResults.ParseResponseResult result = Ledger.parseGetCredDefResponse(credDefResponseJson).get();
                credDefs.put(credDefinationId, new JSONObject(result.getObjectJson()));
            } catch (IndyException e) {
                Log.e(TAG, "createProof: unable to get credentialDefination", e);
            }

        }
        closePool();


        String proofJson = Anoncreds.proverCreateProof(
                wallet,
                proofRequest,
                credentials.toString(),
                MASTER_SECRET_KEY,
                schemas.toString(),
                credDefs.toString(),
                "{}").get();

        Proof proof = new Proof();
        proof.setProofJson(proofJson);
        proof.setSchemaIds(schemaIds);
        proof.setCredDefinationIds(credentialDefIds);


        ProofResult result = new ProofResult();
        result.setProof(proof);
        result.setProofRequestJsonData(proofRequest);
        return result;


    }
}
