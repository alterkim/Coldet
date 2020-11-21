package com.coldblock.coldet.nfc.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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

    private byte[] serializedTransaction;
    private TagNfcDialogListener tagNfcDialogListener;


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

        Intent data = new Intent(TagNfcDialog.this, NfcActivity.class);
        data.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
        startActivityForResult(data, REQUEST_NFC_TRANSMISSION);
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
}
