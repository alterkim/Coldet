package com.coldblock.coldet.nfc.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.widget.TextView;

import com.coldblock.coldet.R;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;

public class NfcActivity extends Activity {

    private byte[] SerializedUnsignedTransaction;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;

    private TextView tv_test;

    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_nfc);

        tv_test = findViewById(R.id.test_tv);

        Intent intent = getIntent();
        SerializedUnsignedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getService(this, 0, nfcIntent, 0);
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag != null) {
            byte[] tagId = tag.getId();
            tv_test.setText("UID:" + toHexString(tagId));
        }
    }

    public static final String CHARS = "0123456789ABCDEF";

    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0F))
                    .append(CHARS.charAt(data[i] & 0x0F));
        }
        return sb.toString();
    }
}