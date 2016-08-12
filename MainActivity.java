package com.farhanahmed.nfc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.farhanahmed.nfclibrary.NfcManager;

public class MainActivity extends AppCompatActivity {
    NfcManager nfcManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nfcManager = new NfcManager(this);
        nfcManager.setDebug(true);


    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.enableForegroundDispatch(this.getClass());
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcManager.disableForegroundDispatch();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        nfcManager.onNewIntent(intent);
        //nfcManager.writeContent("nfc manager made nfc operation easy.");
        Toast.makeText(this,nfcManager.readContent(),Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        nfcManager.release();
    }
}
