package com.coldblock.coldet.wallet;

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

    static final int REQUEST_FOR_ADDRESS = 2;

    private EditText fromQrAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

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

        fromQrAddress = findViewById(R.id.edit_address);
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
