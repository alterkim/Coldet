package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.coldblock.coldet.R;
import com.coldblock.coldet.wallet.barcode.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class CreateTransactionActivity extends Activity {
    public static int REQUEST_FOR_TO_ADDRESS = 1010;

    private EditText toAddress;
    private EditText fromAddress;
    private EditText value;
    private EditText nid;

    @Override
    protected void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_create_transaction);

        Intent intent = getIntent();

        toAddress = findViewById(R.id.et_toAddress);

        fromAddress = findViewById(R.id.et_fromAddress);
        fromAddress.setText(intent.getExtras().getString(WalletAddedActivity.Address));

        value = findViewById(R.id.et_value);
        nid = findViewById(R.id.et_nid);


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
                // TODO: Send transaction data to previous activity
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
                    toAddress.setText(barcode.displayValue);
                }
            }
        }
    }
}
