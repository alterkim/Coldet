package com.coldblock.coldet.wallet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.coldblock.coldet.Coldet;
import com.coldblock.coldet.R;

public class WalletAddedActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_wallet_added);

        Intent intent = getIntent();

        String wallet_address = intent.getExtras().getString(Coldet.Address);

        Button wallet1 = findViewById(R.id.btn_wallet1);
        wallet1.setText(wallet_address);

    }
}
