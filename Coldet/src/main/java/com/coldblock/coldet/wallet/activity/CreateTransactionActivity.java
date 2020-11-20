package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.coldblock.coldet.R;
import com.coldblock.coldet.icon.SerializedUnsignedTransaction;
import com.coldblock.coldet.wallet.barcode.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

import java.math.BigInteger;

import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.data.IconAmount;

public class CreateTransactionActivity extends Activity {
    public static final int REQUEST_FOR_TO_ADDRESS = 1010;
    public static final String UnsignedTransaction = "unsignedTransaction";

    private EditText et_toAddress;
    private EditText et_fromAddress;
    private EditText et_value;
    private EditText et_nid;

    @Override
    protected void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_create_transaction);

        Intent intent = getIntent();

        et_toAddress = findViewById(R.id.et_toAddress);

        et_fromAddress = findViewById(R.id.et_fromAddress);
        et_fromAddress.setText(intent.getExtras().getString(WalletAddedActivity.Address));

        et_value = findViewById(R.id.et_value);
        et_nid = findViewById(R.id.et_nid);


        ImageButton from_qr = findViewById(R.id.btn_open_qr_to);
        from_qr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreateTransactionActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.ICX_Address.name());
                startActivityForResult(intent, REQUEST_FOR_TO_ADDRESS);
            }
        });

        Button btn_confirm = findViewById(R.id.btn_transaction_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Transaction transaction = createTransaction();
                SerializedUnsignedTransaction unsignedTransaction = new SerializedUnsignedTransaction(transaction);

                Intent data = new Intent();
                data.putExtra(UnsignedTransaction, unsignedTransaction);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == REQUEST_FOR_TO_ADDRESS) {
            if(resultCode == CommonStatusCodes.SUCCESS) {
                if(data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    et_toAddress.setText(barcode.displayValue);
                }
            }
        }
    }


    /**
     * Create transaction by input data
     * @return transaction
     */
    private Transaction createTransaction() {
        long timestamp = System.currentTimeMillis() * 1000L;
        Address fromAddress = new Address(et_fromAddress.getText().toString());
        Address toAddress = new Address(et_toAddress.getText().toString());
        BigInteger networkId = new BigInteger(et_nid.getText().toString());
        BigInteger value = IconAmount.of(et_value.getText().toString(), IconAmount.Unit.ICX).toLoop();
        BigInteger stepLimit = new BigInteger("1000000");

        return TransactionBuilder.newBuilder()
                .nid(networkId)
                .from(fromAddress)
                .to(toAddress)
                .value(value)
                .stepLimit(stepLimit)
                .timestamp(new BigInteger(Long.toString(timestamp)))
                .build();
    }


}
