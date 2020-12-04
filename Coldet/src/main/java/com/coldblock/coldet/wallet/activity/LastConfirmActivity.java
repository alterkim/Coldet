package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.coldblock.coldet.R;
import com.coldblock.coldet.icon.SerializedUnsignedTransaction;
import com.coldblock.coldet.network.Data;
import com.coldblock.coldet.nfc.activity.NfcActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.TimeUnit;

import foundation.icon.icx.Callback;
import foundation.icon.icx.IconService;
import foundation.icon.icx.Request;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.data.Bytes;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import okhttp3.OkHttpClient;

public class LastConfirmActivity extends Activity {
    private static final String TAG = "LastConfirmActivity";

    private byte[] serializedSignedTransaction;

    private SignedTransaction signedTransaction;
    private SerializedUnsignedTransaction unsignedTransaction;

    private IconService iconService;

    public static String TXHASH = "txHash";

    @Override
    protected void onCreate(Bundle savedBundle){
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_last_confirm);

        OkHttpClient httpClient = new OkHttpClient.Builder()
                .readTimeout(1000, TimeUnit.MILLISECONDS)
                .writeTimeout(600, TimeUnit.MILLISECONDS)
                .build();
        iconService = new IconService(new HttpProvider(httpClient, Data.YEOUIDO_TESTNET, 3));

        Intent intent = getIntent();
        serializedSignedTransaction = intent.getByteArrayExtra(NfcActivity.SerializedSignTransaction);
        unsignedTransaction = (SerializedUnsignedTransaction) intent.getSerializableExtra(WalletAddedActivity.TransactionInfo);


        signedTransaction = DeserializeSignTransaction(serializedSignedTransaction);


        EditText toAddress = findViewById(R.id.lc_toAddress);
        EditText fromAddress = findViewById(R.id.lc_fromAddress);
        EditText value = findViewById(R.id.lc_value);
        toAddress.setText(unsignedTransaction.getTo());
        fromAddress.setText(unsignedTransaction.getFrom());
        value.setText(unsignedTransaction.getValue().toString());
        Button btn_confirm = findViewById(R.id.btn_last_confirm);
        Button btn_cancel = findViewById(R.id.btn_cancel);

        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendTransaction(signedTransaction);
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                setResult(CommonStatusCodes.CANCELED, data);
                finish();
            }
        });
    }

    private SignedTransaction DeserializeSignTransaction(byte[] serializedSignedTransaction) {
        SignedTransaction signedTransaction = new SignedTransaction();
        try(ByteArrayInputStream bais = new ByteArrayInputStream(serializedSignedTransaction)) {
            try(ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object object = ois.readObject();
                signedTransaction.setProperties((RpcObject) object);
                Log.d(TAG, "Deserialized: " + signedTransaction.getProperties().toString());
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return signedTransaction;
    }

    private void SendTransaction(SignedTransaction signedTransaction) {
        Request<Bytes> request = iconService.sendTransaction(signedTransaction);

        request.execute(new Callback<Bytes>() {
            @Override
            public void onSuccess(Bytes result) {
                Log.d(TAG, "The transaction is confirmed");
                Log.d(TAG, "TxHash: " + result.toString());
//                Toast.makeText(getApplicationContext(), "거래가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                Intent data = new Intent();
                data.putExtra(TXHASH, result.toString());
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d(TAG, "The transaction is failed");
//                Toast.makeText(getApplicationContext(), "거래에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        });
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
