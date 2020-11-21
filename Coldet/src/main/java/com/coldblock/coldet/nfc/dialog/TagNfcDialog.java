package com.coldblock.coldet.nfc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.coldblock.coldet.R;
import com.coldblock.coldet.nfc.service.NfcService;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;

public class TagNfcDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private byte[] serializedTransaction;
    private TagNfcDialogListener tagNfcDialogListener;

    public TagNfcDialog(Context context, byte[] serializedTransaction) {
        super(context);
        this.context = context;
        this.serializedTransaction = serializedTransaction;
    }

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

        Button button = findViewById(R.id.btn_nfc_confirm);
        button.setOnClickListener(this);

        Intent intent = new Intent(getContext(), NfcService.class);
        intent.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
        context.startService(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_nfc_confirm) {
            String test = "Done";
            tagNfcDialogListener.getSignedTransaction(test);
            dismiss();
        }
    }
}
