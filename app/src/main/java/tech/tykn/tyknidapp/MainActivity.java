package tech.tykn.tyknidapp;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.system.ErrnoException;
import android.system.Os;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import org.hyperledger.indy.sdk.IndyException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import tech.tykn.tyknid.Service;
import tech.tykn.tyknid.WalletAlreadyExistException;
import tech.tykn.tyknid.WalletCreationException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final Service tyknService = new Service();
        super.onCreate(savedInstanceState);
        try {
            Os.setenv("EXTERNAL_STORAGE", getExternalFilesDir(null).getAbsolutePath(), true);
            Os.setenv("RUST_LOG","TRACE",true);
        } catch (ErrnoException e) {

            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("logcat -d tech.tykn.tyknidapp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));

        StringBuilder log=new StringBuilder();
        String line = "";
        while (true) {
            try {
                if (!((line = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.append(line);
        }
        TextView logbox = (TextView)findViewById(R.id.logbox);
        logbox.setText(log.toString());
        logbox.setMovementMethod(new ScrollingMovementMethod());

        Button btnCreateWallet = findViewById(R.id.btnCreateWallet);
        btnCreateWallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tyknService.createWallet("testWallet","test");
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
                    tyknService.deleteWallet("testWallet","test");
                } catch ( IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btngenerateDID = findViewById(R.id.btngenerateDID);
        btngenerateDID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    tyknService.generatePADID("testWallet","test");
                } catch ( IndyException | InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        Button btnRqstCred = findViewById(R.id.btnRqstCred);
        btnRqstCred.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//                try {
//                    tyknService.createCredentialRequest("testWallet","test");
//                } catch ( IndyException | InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
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
}
