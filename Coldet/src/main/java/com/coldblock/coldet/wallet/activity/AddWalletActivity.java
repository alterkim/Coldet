package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.coldblock.coldet.R;
import com.coldblock.coldet.wallet.barcode.BarcodeCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;

public class AddWalletActivity extends Activity {

    public static final int REQUEST_FOR_ADDRESS = 1002;
    public static final String Address = "Address";

    private EditText fromQrAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        fromQrAddress = findViewById(R.id.edit_address);

        ImageButton setQr = findViewById(R.id.btn_open_qr);
        setQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddWalletActivity.this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                intent.putExtra(BarcodeCaptureActivity.PARAM_SCANTYPE, BarcodeCaptureActivity.ScanType.ICX_Address.name());
                startActivityForResult(intent, REQUEST_FOR_ADDRESS);
            }
        });

        Button confirm = findViewById(R.id.btn_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Address, fromQrAddress.getText().toString());
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode == REQUEST_FOR_ADDRESS) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null){
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    fromQrAddress.setText(barcode.displayValue);
                }
            }
        }
    }
}
