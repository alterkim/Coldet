package com.coldblock.coldet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;

import com.coldblock.coldet.wallet.activity.AddWalletActivity;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

public class Coldet extends Activity {
    public static int GET_ADDRESS_BY_QR_CODE = 1001;
//    public static int ADD_WALLET_ADDRESS = 1003;
    public static String Address = "address";

    private String address_Wallet1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        ImageButton add_wallet = findViewById(R.id.btn_add_wallet);
        add_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Coldet.this, AddWalletActivity.class);
                startActivityForResult(intent,GET_ADDRESS_BY_QR_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_ADDRESS_BY_QR_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    address_Wallet1 = data.getExtras().getString(AddWalletActivity.Address);
                    Intent intent = new Intent(Coldet.this, WalletAddedActivity.class);
                    intent.putExtra(Address, address_Wallet1);
                    startActivity(intent);
                }
            }
        }
    }
}
