package com.coldblock.coldet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageButton;

import com.coldblock.coldet.wallet.AddWalletActivity;

public class Coldet extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ImageButton add_wallet = findViewById(R.id.btn_add_wallet);
        add_wallet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Coldet.this, AddWalletActivity.class);
                startActivity(intent);
            }
        });
    }
}
