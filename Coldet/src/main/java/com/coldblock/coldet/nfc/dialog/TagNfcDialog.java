package com.coldblock.coldet.nfc.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.coldblock.coldet.R;
import com.coldblock.coldet.nfc.activity.NfcActivity;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

public class TagNfcDialog extends Activity implements View.OnClickListener {

    public static final int REQUEST_NFC_TRANSMISSION = 1020;
    public static final String NFC_ADAPTER = "nfcAdapter";

    private byte[] serializedTransaction;
    private TagNfcDialogListener tagNfcDialogListener;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;


    public void setDialogListener(TagNfcDialogListener tagNfcDialogListener) {
        this.tagNfcDialogListener = tagNfcDialogListener;
    }


    @Override
    protected void onCreate(Bundle savedBundle){
        super.onCreate(savedBundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tag_nfc);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        Intent intent = getIntent();
        serializedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);

        Button button = findViewById(R.id.btn_nfc_confirm);
        button.setOnClickListener(this);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
    }

    @Override
    public void onClick(View v) {
//        if (v.getId() == R.id.btn_nfc_confirm) {
//            String test = "Done";
//            tagNfcDialogListener.getSignedTransaction(test);
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == REQUEST_NFC_TRANSMISSION) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    System.out.println("Finish NfcActivity");
                }
            }
        }
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
            System.out.println("TagID: " + toHexString(tagId));

            ProcessNFC();
        }
    }

    private void ProcessNFC(){
        Intent data = new Intent(TagNfcDialog.this, NfcActivity.class);
        data.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
        startActivityForResult(data, REQUEST_NFC_TRANSMISSION);
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
