package tech.tykn.tyknidapp;

import android.os.Bundle;
import android.system.ErrnoException;
import android.system.Os;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


import org.hyperledger.indy.sdk.IndyException;
import org.hyperledger.indy.sdk.anoncreds.AnoncredsResults;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import tech.tykn.tyknid.DID;
import tech.tykn.tyknid.Proof;
import tech.tykn.tyknid.ProofResult;
import tech.tykn.tyknid.Service;
import tech.tykn.tyknid.WalletAlreadyExistException;
import tech.tykn.tyknid.WalletCreationException;

public class MainActivity extends AppCompatActivity {

    private String TAG = "Tykn::APP>>>> ";

    private File createConfigFile(String testPoolIp) throws IOException {
        File configFile = File.createTempFile("pool_config", ".txn", this.getBaseContext().getCacheDir());
        String path = configFile.getAbsolutePath();
        String[] defaultTxns = new String[]{
                String.format("{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node1\",\"blskey\":\"4N8aUNHSgjQVgkpm8nhNEfDf6txHznoYREg9kirmJrkivgL4oSEimFF6nsQ6M41QvhM2Z33nves5vfSn9n1UwNFJBYtWVnHYMATn76vLuL3zU88KyeAYcHfsih3He6UHcXDxcaecHVz6jhCYz1P2UZn2bDVruL5wXpehgBfBaLKm3Ba\",\"blskey_pop\":\"RahHYiCvoNCtPTrVtP7nMC5eTYrsUA8WjXbdhNc8debh1agE9bGiJxWBXYNFbnJXoXhWFMvyqhqhRoq737YQemH5ik9oL7R4NTTCz2LEZhkgLJzB3QRQqJyBNyv7acbdHrAT8nQ9UkLbaVL9NBpnWXBTw4LEMePaSHEw66RzPNdAX1\",\"client_ip\":\"%s\",\"client_port\":9702,\"node_ip\":\"%s\",\"node_port\":9701,\"services\":[\"VALIDATOR\"]},\"dest\":\"Gw6pDLhcBcoQesN72qfotTgFa7cbuqZpkX3Xo6pLhPhv\"},\"metadata\":{\"from\":\"Th7MpTaRZVRYnPiabds81Y\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":1,\"txnId\":\"fea82e10e894419fe2bea7d96296a6d46f50f93f9eeda954ec461b2ed2950b62\"},\"ver\":\"1\"}", testPoolIp, testPoolIp),
                String.format("{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node2\",\"blskey\":\"37rAPpXVoxzKhz7d9gkUe52XuXryuLXoM6P6LbWDB7LSbG62Lsb33sfG7zqS8TK1MXwuCHj1FKNzVpsnafmqLG1vXN88rt38mNFs9TENzm4QHdBzsvCuoBnPH7rpYYDo9DZNJePaDvRvqJKByCabubJz3XXKbEeshzpz4Ma5QYpJqjk\",\"blskey_pop\":\"Qr658mWZ2YC8JXGXwMDQTzuZCWF7NK9EwxphGmcBvCh6ybUuLxbG65nsX4JvD4SPNtkJ2w9ug1yLTj6fgmuDg41TgECXjLCij3RMsV8CwewBVgVN67wsA45DFWvqvLtu4rjNnE9JbdFTc1Z4WCPA3Xan44K1HoHAq9EVeaRYs8zoF5\",\"client_ip\":\"%s\",\"client_port\":9704,\"node_ip\":\"%s\",\"node_port\":9703,\"services\":[\"VALIDATOR\"]},\"dest\":\"8ECVSk179mjsjKRLWiQtssMLgp6EPhWXtaYyStWPSGAb\"},\"metadata\":{\"from\":\"EbP4aYNeTHL6q385GuVpRV\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":2,\"txnId\":\"1ac8aece2a18ced660fef8694b61aac3af08ba875ce3026a160acbc3a3af35fc\"},\"ver\":\"1\"}", testPoolIp, testPoolIp),
                String.format("{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node3\",\"blskey\":\"3WFpdbg7C5cnLYZwFZevJqhubkFALBfCBBok15GdrKMUhUjGsk3jV6QKj6MZgEubF7oqCafxNdkm7eswgA4sdKTRc82tLGzZBd6vNqU8dupzup6uYUf32KTHTPQbuUM8Yk4QFXjEf2Usu2TJcNkdgpyeUSX42u5LqdDDpNSWUK5deC5\",\"blskey_pop\":\"QwDeb2CkNSx6r8QC8vGQK3GRv7Yndn84TGNijX8YXHPiagXajyfTjoR87rXUu4G4QLk2cF8NNyqWiYMus1623dELWwx57rLCFqGh7N4ZRbGDRP4fnVcaKg1BcUxQ866Ven4gw8y4N56S5HzxXNBZtLYmhGHvDtk6PFkFwCvxYrNYjh\",\"client_ip\":\"%s\",\"client_port\":9706,\"node_ip\":\"%s\",\"node_port\":9705,\"services\":[\"VALIDATOR\"]},\"dest\":\"DKVxG2fXXTU8yT5N7hGEbXB3dfdAnYv1JczDUHpmDxya\"},\"metadata\":{\"from\":\"4cU41vWW82ArfxJxHkzXPG\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":3,\"txnId\":\"7e9f355dffa78ed24668f0e0e369fd8c224076571c51e2ea8be5f26479edebe4\"},\"ver\":\"1\"}", testPoolIp, testPoolIp),
                String.format("{\"reqSignature\":{},\"txn\":{\"data\":{\"data\":{\"alias\":\"Node4\",\"blskey\":\"2zN3bHM1m4rLz54MJHYSwvqzPchYp8jkHswveCLAEJVcX6Mm1wHQD1SkPYMzUDTZvWvhuE6VNAkK3KxVeEmsanSmvjVkReDeBEMxeDaayjcZjFGPydyey1qxBHmTvAnBKoPydvuTAqx5f7YNNRAdeLmUi99gERUU7TD8KfAa6MpQ9bw\",\"blskey_pop\":\"RPLagxaR5xdimFzwmzYnz4ZhWtYQEj8iR5ZU53T2gitPCyCHQneUn2Huc4oeLd2B2HzkGnjAff4hWTJT6C7qHYB1Mv2wU5iHHGFWkhnTX9WsEAbunJCV2qcaXScKj4tTfvdDKfLiVuU2av6hbsMztirRze7LvYBkRHV3tGwyCptsrP\",\"client_ip\":\"%s\",\"client_port\":9708,\"node_ip\":\"%s\",\"node_port\":9707,\"services\":[\"VALIDATOR\"]},\"dest\":\"4PS3EDQ3dW1tci1Bp6543CfuuebjFrg36kLAUcskGfaA\"},\"metadata\":{\"from\":\"TWwCRQRZ2ZHMJFn9TzLp7W\"},\"type\":\"0\"},\"txnMetadata\":{\"seqNo\":4,\"txnId\":\"aa5e817d7cc626170eca175822029339a444eb0ee8f0bd20d3b0b76e566fb008\"},\"ver\":\"1\"}", testPoolIp, testPoolIp)
        };
        File file = new File(path);

        FileWriter fw = new FileWriter(file);
        for (String defaultTxn : defaultTxns) {
            fw.write(defaultTxn);
            fw.write("\n");
        }

        fw.close();

        return file;
    }

    File configFile = null;
    String padid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            configFile = createConfigFile("11.0.0.2");
        } catch (IOException e) {
            e.printStackTrace();
        }
        final Service tyknService = new Service();
        super.onCreate(savedInstanceState);
        try {
            Os.setenv("EXTERNAL_STORAGE", getExternalFilesDir(null).getAbsolutePath(), true);
            Os.setenv("RUST_LOG", "DEBUG", true);
        } catch (ErrnoException e) {

            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);

        final TextView statusBox = findViewById(R.id.statusBox);
        Button btnCreateWallet = findViewById(R.id.btnCreateWallet);
        btnCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tyknService.createWallet("testWallet", "test");
                    statusBox.setText(String.format("Create wallet"));
                } catch (WalletCreationException | WalletAlreadyExistException | ExecutionException | InterruptedException | IndyException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnDeleteWallet = findViewById(R.id.btnDeleteWallet);
        btnDeleteWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tyknService.deleteWallet("testWallet", "test");
                    statusBox.setText(String.format("Delete wallet"));
                } catch (IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btngenerateDID = findViewById(R.id.btngenerateDID);
        btngenerateDID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    DID did = tyknService.generatePADID("testWallet", "test");
                    padid = did.getDID();
                    statusBox.setText(String.format("DID: %s \nVerKey: %s", did.getDID(), did.getVerkey()));
                } catch (IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        Button btnRqstCred = findViewById(R.id.btnRqstCred);
        btnRqstCred.setOnClickListener(new View.OnClickListener() {
            String credDefId = "9sCNCi3gvzZmjJrpa3NTJn:3:CL:12:age_proof";
            String offerJson = "{\"schema_id\":\"Th7MpTaRZVRYnPiabds81Y:2:android_name_age:3.0\",\"cred_def_id\":\"9sCNCi3gvzZmjJrpa3NTJn:3:CL:12:age_proof\",\"key_correctness_proof\":{\"c\":\"11832162348636765951219241282701389006797179542831840655822751145759841466537\",\"xz_cap\":\"109011072221367687098749395984201262100083810349349765957064819381741350608020452060687751356800662048159515232410781929672629473219170357121671843521789161265067826290259529365431128862648941982323936114629030813106779438086494520267293676546248917930874867337653513817268639679835042039136459426123718626634252434644974395731082618060178665820016253160829871795253453398312886790443606017737461975222321691275147367649932797414270948963890488811076036819722629018973290135282191432761483017611695327695861889920424506042238399901014916055578155548630106227144308937925116464791165862969323611094110492288539455121591388640433017323126553440599875224658610437530895399906812819294471920107892\",\"xr_cap\":[[\"name\",\"42216909325203858065139496033290457775677510736483054824460006663650220063050724778992943646930511107243541163470020995461500049177882432054777322602597121734469156502108798894100570640519698306014332974730388551481269568041747754375112993361923602747148076480023442523371917675337792610357338681155111523708860755770563738570578403836358139642133884499856687367136749740810069763815291334753316774888361711030300061372955853586664940840806458204967921527640957338077195945701061559869210116498290508989023853818470160567474317705334463083556589738146608172457004375818398297917069431942381148540602940004274686018102342023420473090371013776058643199657702882400023711087476523092651416477830\"],[\"age\",\"197340289158067801483294748069548237998351287217659985522100492478188089344434706740940500980895066448032920179769149872393475754307870833871825397783260875763000123736654806879453814249894532356476528945892518854282077638875974176873503024354115543000396019440866548680294756138133959538963647301787694069534909282263778022022599820679102784918354718743328043153596799523073214576586759540946635091597763261682505129460603188009516527933499115788703040597235677856254534115142442159866039478485181579927998972322958777977965897719569236524576370543911728289652763093875435642426037407766046857466939992684243495520507262529566834844935148481328658563934137934391709142181694502847321155062151\"],[\"master_secret\",\"267573898307790440687623457551680501146385048485667096293107062234375779462167455074290029409489853916002620561253017145834846286074844554381436648280522845618121982526578672392155007210032414629984922089675114310357781743461438358412005801151525669936529730056776017621482349690032990346371602406264429473060986823050646270794135872778307282688144530254104210285118901456226359502562446979164040210217432092243527332780289194858328071467421981724920292807512374484282421820707614903178729087757077755236281697239844012936992508090837912782458349843717987296135659134251614858197513305289083853231768649580598961915145549005836089813196040360352748340804074959927935205867769098638823635933104\"]]},\"nonce\":\"857602311005304197229676\"}";

            @Override
            public void onClick(View view) {
                try {
                    AnoncredsResults.ProverCreateCredentialRequestResult res = tyknService.createCredentialRequest("testWallet", "test", configFile, padid, credDefId, offerJson);
                    statusBox.setText(String.format("json: %s \n metadata: %s", res.getCredentialRequestJson(), res.getCredentialRequestMetadataJson()));
                    Log.i(TAG, String.format("json: %s \n metadata: %s", res.getCredentialRequestJson(), res.getCredentialRequestMetadataJson()));
                } catch (IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

            }
        });

        Button btnAcceptCred = findViewById(R.id.btnAcceptCred);
        btnAcceptCred.setOnClickListener(new View.OnClickListener() {
            String credDefId = "9sCNCi3gvzZmjJrpa3NTJn:3:CL:12:age_proof";
            String credentialreqMetadataJson = "{\"master_secret_blinding_data\":{\"v_prime\":\"987241981576125360862894894785752796057004208454016406454633881896300890343281794284320162199774770806228841534409420755830230442030287256122652835280363556351661447617384604091000222371033886903135089362386983311450444678558348903166515486157569578409876622947771417527248662364655771939184425679370952708951115738081963362909640902811012837391398265385653113308971845848147734215508850748300020606976220772743152712610521633975418153442840607710922928162013631821667495725385297085440781195225911967073412720986239268185348905102535377729282876456515080898844037998523042446270864244832308490121746611237675679293856091618479672089257051\",\"vr_prime\":null},\"nonce\":\"97905135957546096655940\",\"master_secret_name\":\"masterSecret\"}";
            String credential = "{\"schema_id\":\"Th7MpTaRZVRYnPiabds81Y:2:android_name_age:3.0\",\"cred_def_id\":\"9sCNCi3gvzZmjJrpa3NTJn:3:CL:12:age_proof\",\"rev_reg_id\":null,\"values\":{\"age\":{\"raw\":\"23\",\"encoded\":\"23\"},\"name\":{\"raw\":\"sami\",\"encoded\":\"7092402453723823421\"}},\"signature\":{\"p_credential\":{\"m_2\":\"62580369015297556301338127040423873012772385096174708503349661119808537469054\",\"a\":\"42671248964055958447962939983518416739213750113267004975708828260699109448343650817818075275222636732826329034574577609903970465395737838309869614592228716695254050001327985448232611746374958010004719534088549512507500161378158231072092929798330503455299281097798761308306443784864127430584458813522960689436851027973957549769532589017627518570807515993856849505743565692578096996437865708823497598012829530189067229862645779797454420100312852388815653307011550187439048751566451939943594590327542070395222925934086976947191592913975245133014300644360074734163813938854162550600023778393950256196615827551639570116514\",\"e\":\"259344723055062059907025491480697571938277889515152306249728583105665800713306759149981690559193987143012367913206299323899696942213235956742930302696598465421948002518671721159443\",\"v\":\"5540610469395762744926930383905895552899801896676861146580300504950305288631341929871828342577769723553281189038121391101044317989375886034716685250758199263897863493430489907064516061101039819548333802843081665339437752691958159074208096700620294328514692569684444160697520275966474646895583955521384916773561049046606155101220719864033361260669690794734469960258733734862048956837213184315727886915632057875119655320223103994872486694059966114036162419254904124045298157215688468369267528402991610681292191948891365883067493050245600906467130277966687217401741942187320281308382952010790369717218196620833462437978120855310032081382243708123880722981326038012232546587563068310297955682608979049051763965278434874737194122296094133695784834923872732936055876906682699069147382196689336300641567304330163573788111556896\"},\"r_credential\":null},\"signature_correctness_proof\":{\"se\":\"13001548365153151747246253718210028876822698702531306626203673583953982449598074564956611208778536005309502796263444628019348054266335869039526639081144683281751013722920538291015265035350291130583158878705347162596124275145674073314898472385558057324951917445979512875500589232568886733971780962288307364497213364346348115327217717239093053368916780162952415715697960623971823605108082643610664779946822851228800769603964036633764421144253591643043598862221913904210330037805266012828080827866075022922732914529517834962860206989699338177551176391450186836887880876556006955690590130885037110792243808578247727350788\",\"c\":\"6883834279766917038803722607339187685208105658289726448443780970204447005945\"},\"rev_reg\":null,\"witness\":null}";

            @Override
            public void onClick(View view) {
                try {
                    tyknService.saveCredential("testWallet", "test", configFile, "5h9ihnzuHJMR5xPBY85b6M", credDefId, credentialreqMetadataJson, credential);
                    statusBox.setText("credential saved!");
                } catch (IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnCreateProof = findViewById(R.id.btnCreateProof);
        btnCreateProof.setOnClickListener(new View.OnClickListener() {
            String credDefId = "9sCNCi3gvzZmjJrpa3NTJn:3:CL:12:age_proof";
            String proofRequest = "{\"nonce\": \"123\",\"name\": \"proof_req_1\",\"version\": \"0.1\",\"requested_predicates\":{},\"non_revoked\":{},\"requested_attributes\": {\"attr1_referent\": {\"name\": \"name\"}}}";

            @Override
            public void onClick(View view) {
                try {
                    ProofResult proof = tyknService.createProof("testWallet", "test", configFile, credDefId, proofRequest);
                    String displaytext = String.format("proofJson: %s schemaIds: %s credDefids: %s", proof.getProof().getProofJson(), proof.getProof().getSchemaIds(), proof.getProof().getCredDefinationIds());
                    Log.i(TAG, displaytext);
                    statusBox.setText(displaytext);
                } catch (IndyException | InterruptedException | ExecutionException | JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnRunTestSeq = findViewById(R.id.runTestSequence);
        btnRunTestSeq.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            doEverything(tyknService);
                        } catch (JSONException | InterruptedException | ExecutionException | IndyException | WalletAlreadyExistException | WalletCreationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };

                thread.start();


            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public String getTyknImsUrl(String path){
        String TyknImsIp = "http://11.0.0.3:50001";
        return TyknImsIp+path;
    }

    public String getOrgImsUrl(String path){
        String OrgIms = "http://11.0.0.4:50002";
        return OrgIms+path;
    }
    public void doEverything(Service tyknService) throws JSONException, InterruptedException, ExecutionException, IndyException, WalletAlreadyExistException, WalletCreationException, IOException {

        Log.d(TAG, "calling " + getTyknImsUrl("/api/schema"));
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();




        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body0 = RequestBody.create(mediaType, "{\"name\": \"android_name_age\", \"version\": \"3.0\",\n   \"attributes\": [   \"name\",\"age\" ] \n }");
        Request schemarequest = new Request.Builder()
                .url(getTyknImsUrl("/api/schema"))
                .post(body0)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Host", "11.0.0.3:50001")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Content-Length", "106")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        Response schemaResponse = client.newCall(schemarequest).execute();
        Log.d(TAG, "received " + schemaResponse);
        String schemaId = new JSONObject(schemaResponse.body().string()).getString("schema_id");

        Log.d(TAG, "calling  "+ getOrgImsUrl("/api/credential/definition"));
        RequestBody body1 = RequestBody.create(mediaType, "{\"name\": \"age_proof\",\n   \"schema_id\": \"" + schemaId + "\"\n }");
        Request createCedDefrequest = new Request.Builder()
                .url("http://11.0.0.4:50002/api/credential/definition")
                .post(body1)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Host", "11.0.0.4:50002")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Content-Length", "93")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        Response createCedDefResponse = client.newCall(createCedDefrequest).execute();
        Log.d(TAG, "received " + createCedDefResponse);
        String credDefId = new JSONObject(createCedDefResponse.body().string()).getString("credential_definition_id");

        Log.d(TAG, "calling"+ getOrgImsUrl("/api/credential/credoffer"));
        RequestBody body2 = RequestBody.create(mediaType, "{\"credDefID\":  \"" + credDefId + "\", \"correlation\": {  \"correlationID\": \"android\"} \n }");
        Request createCredOfferrequest = new Request.Builder()
                .url(getOrgImsUrl("/api/credential/credoffer"))
                .post(body2)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Content-Length", "128")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        Response createCredOfferResponse = client.newCall(createCredOfferrequest).execute();
        Log.d(TAG, "recevide " + createCredOfferResponse);
        String credOffer = new JSONObject(createCredOfferResponse.body().string()).getString("credOfferJsonData");

        String walletName = "test";
        String walletKey = "test";
        try{
            tyknService.deleteWallet(walletName, walletKey);
        }catch (Exception ex){
            Log.e(TAG, "wallet not found to be deleted", ex );
        }

        tyknService.createWallet(walletName, walletKey);
        DID paDID = tyknService.generatePADID(walletName, walletKey);
        AnoncredsResults.ProverCreateCredentialRequestResult credReqFromPA = tyknService.createCredentialRequest(walletName, walletKey, configFile, paDID.getDID(), credDefId, credOffer);


        Log.d(TAG, "calling " + getOrgImsUrl("/api/credential/issue"));
        RequestBody body3 = RequestBody.create(mediaType, "{ \"credOfferJsonData\": \"" + credOffer.replace("\"","\\\"") + "\"," +
                "\"credentialRequest\": \"" + credReqFromPA.getCredentialRequestJson().replace("\"","\\\"") + "\"," +
                "\"correlation\": {  \"correlationID\": \"test\"}," +
                "\"attributes\": {\"name\":\"sami\",\"age\":23} \n }");
        Request issueCredrequest = new Request.Builder()
                .url(getOrgImsUrl("/api/credential/issue"))
                .post(body3)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("Accept-Encoding", "gzip, deflate")
                .addHeader("Content-Length", "5256")
                .addHeader("Connection", "keep-alive")
                .addHeader("cache-control", "no-cache")
                .build();

        Response issueCredResponse = client.newCall(issueCredrequest).execute();

        String credJson = new JSONObject(issueCredResponse.body().string()).getString("credential");

        Log.d(TAG, "received " + issueCredResponse);
        tyknService.saveCredential(walletName, walletKey, configFile, paDID.getDID(), credDefId, credReqFromPA.getCredentialRequestMetadataJson(), credJson);
        String proofRequest = "{\"nonce\": \"123\",\"name\": \"proof_req_1\",\"version\": \"0.1\",\"requested_predicates\":{},\"non_revoked\":{},\"requested_attributes\": {\"attr1_referent\": {\"name\": \"name\"}}}";
        Proof proof = tyknService.createProof(walletName,walletKey,configFile,"U8iuDVKHUXRYRCRPDwqTsu:3:CL:12:age_proof", proofRequest).getProof();
        Log.d(TAG, "proof : >> " + proof.getProofJson());

        Log.d(TAG, "-->> TEST SUITE COMPLETE <<--");

    }


}
