package com.coldblock.coldet.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.coldblock.coldet.R;
import com.coldblock.coldet.wallet.barcode.BarcodeCaptureActivity;

public class AddWalletActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallet);

        // TODO: Make QR code button
        Button setQR = findViewById(R.id.btn_confirm);
        setQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddWalletActivity.this, BarcodeCaptureActivity.class);
                startActivity(intent);
            }
        });
    }
}
