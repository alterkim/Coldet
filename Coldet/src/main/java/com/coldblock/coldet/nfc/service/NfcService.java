package com.coldblock.coldet.nfc.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.coldblock.coldet.wallet.activity.WalletAddedActivity;

import java.util.Arrays;

public class NfcService extends Service {

    private byte[] SerializedUnsignedTransaction;
    public NfcService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SerializedUnsignedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);
        System.out.println(Arrays.toString(SerializedUnsignedTransaction));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}