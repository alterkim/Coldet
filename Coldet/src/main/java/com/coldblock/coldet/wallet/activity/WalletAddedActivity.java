package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.coldblock.coldet.Coldet;
import com.coldblock.coldet.R;
import com.coldblock.coldet.icon.SerializedUnsignedTransaction;
import com.coldblock.coldet.nfc.activity.NfcActivity;
import com.coldblock.coldet.nfc.dialog.TagNfcDialog;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class WalletAddedActivity extends Activity {
    private static final String TAG = "WalletAddedActivity";

    public static final int REQUEST_CREATE_TRANSACTION = 1005;
    public static final int REQUEST_SEND_NFC_TRANSACTION = 1006;
    public static final int REQUEST_LAST_CONFIRM = 1007;
    public static final String Address = "address";
    public static final String Transaction = "transaction";
    public static final String TransactionInfo = "transactionInfo";

    private SerializedUnsignedTransaction unsignedTransaction;
    private byte[] serializedSignedTransaction;

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
                if (data != null){
                    unsignedTransaction = (SerializedUnsignedTransaction) data.getSerializableExtra(CreateTransactionActivity.UnsignedTransaction);
                    sendTransactionByNfc();
                }
            }
        }
        else if (requestCode == REQUEST_SEND_NFC_TRANSACTION) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    serializedSignedTransaction = data.getByteArrayExtra(NfcActivity.SerializedSignTransaction);
                    lastConfirmTransaction(serializedSignedTransaction);
                }
            }
        }
        else if (requestCode == REQUEST_LAST_CONFIRM) {
            if (resultCode ==  CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Log.d(TAG, "Transaction finished");
                    Toast.makeText(getApplicationContext(), "거래가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
            else if(resultCode == CommonStatusCodes.CANCELED) {
                Log.d(TAG, "Transaction canceled");
                Toast.makeText(getApplicationContext(), "거래가 취소되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void lastConfirmTransaction(byte[] serializedSignedTransaction){
        Intent data = new Intent(WalletAddedActivity.this, LastConfirmActivity.class);
        data.putExtra(NfcActivity.SerializedSignTransaction, serializedSignedTransaction);
        data.putExtra(TransactionInfo, unsignedTransaction);
        startActivityForResult(data, REQUEST_LAST_CONFIRM);
    }

    private void sendTransactionByNfc() {
        Intent data = new Intent(WalletAddedActivity.this, TagNfcDialog.class);
        byte[] byteSerializedTransaction = SerializeTransaction(unsignedTransaction);
        data.putExtra(Transaction, byteSerializedTransaction);
        startActivityForResult(data, REQUEST_SEND_NFC_TRANSACTION);
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
