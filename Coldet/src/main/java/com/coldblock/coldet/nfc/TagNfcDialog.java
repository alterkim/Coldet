package com.coldblock.coldet.nfc;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.coldblock.coldet.R;

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

//        Button button = findViewById(R.id.btn_nfc_confirm);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

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
