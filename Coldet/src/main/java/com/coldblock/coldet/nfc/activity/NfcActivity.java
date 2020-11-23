package com.coldblock.coldet.nfc.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.coldblock.coldet.R;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;

public class NfcActivity extends Activity {
    private static final String TAG = "NfcActivity";

    private byte[] SerializedUnsignedTransaction;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private ImageView loading_view;

    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_nfc);

        loading_view = findViewById(R.id.nfc_loading);
        Glide.with(this).load(R.raw.loading).into(loading_view);

        Intent intent = getIntent();
        SerializedUnsignedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);


    }


}