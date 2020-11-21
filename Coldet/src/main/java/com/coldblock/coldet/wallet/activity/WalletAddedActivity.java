package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.coldblock.coldet.Coldet;
import com.coldblock.coldet.R;
import com.coldblock.coldet.icon.SerializedUnsignedTransaction;
import com.coldblock.coldet.nfc.dialog.TagNfcDialog;
import com.coldblock.coldet.nfc.dialog.TagNfcDialogListener;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class WalletAddedActivity extends Activity {
    public static final int REQUEST_CREATE_TRANSACTION = 1005;
    public static final int REQUEST_SEND_NFC_TRANSACTION = 1006;
    public static final String Address = "address";
    public static final String Transaction = "transaction";

    private SerializedUnsignedTransaction unsignedTransaction;

    @Override
    protected void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_wallet_added);

        Intent intent = getIntent();

        String wallet_address = intent.getExtras().getString(Coldet.Address);

        Button wallet1 = findViewById(R.id.btn_wallet1);
        wallet1.setText(wallet_address);
        wallet1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent(WalletAddedActivity.this, CreateTransactionActivity.class);
                data.putExtra(Address, wallet1.getText().toString());
                startActivityForResult(data, REQUEST_CREATE_TRANSACTION);
            }
        });

        Button wallet2 = findViewById(R.id.btn_wallet2);
        wallet2.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if (requestCode == REQUEST_CREATE_TRANSACTION) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null ){
                    unsignedTransaction = (SerializedUnsignedTransaction) data.getSerializableExtra(CreateTransactionActivity.UnsignedTransaction);
                    sendTransactionByNfc();
                }
            }
        }
    }

    private void sendTransactionByNfc() {
        Intent data = new Intent(WalletAddedActivity.this, TagNfcDialog.class);
        byte[] byteSerializedTransaction = SerializeTransaction(unsignedTransaction);
        TagNfcDialog dialog = new TagNfcDialog(this, byteSerializedTransaction);
        dialog.setDialogListener(new TagNfcDialogListener() {
            @Override
            public void getSignedTransaction(String test) {
                System.out.println("get Signed Transaction");
            }
        });
        dialog.show();
    }

    private byte[] SerializeTransaction(SerializedUnsignedTransaction unsignedTransaction) {
        byte[] byteSerializedTransaction = new byte[0];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()){
            try(ObjectOutputStream oos = new ObjectOutputStream(baos)){
                oos.writeObject(unsignedTransaction);
                byteSerializedTransaction = baos.toByteArray();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return byteSerializedTransaction;
    }
}
