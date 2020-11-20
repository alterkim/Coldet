package com.coldblock.coldet.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.coldblock.coldet.Coldet;
import com.coldblock.coldet.R;

public class WalletAddedActivity extends Activity {
    public static final int REQUEST_CREATE_TRANSACTION = 1005;
    public static final String Address = "address";

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


    }
}
