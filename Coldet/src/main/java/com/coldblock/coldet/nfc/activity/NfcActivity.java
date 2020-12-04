package com.coldblock.coldet.nfc.activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.coldblock.coldet.R;
import com.coldblock.coldet.icon.SerializedSignedTransaction;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Array;
import java.util.Arrays;

import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.transport.jsonrpc.RpcObject;

public class NfcActivity extends Activity {
    private static final String TAG = "NfcActivity";

    private byte[] serializedUnsignedTransaction;
    private byte[] serializedSignedTransaction;

    private SignedTransaction signedTransaction;
    private int length;

    private ImageView loading_view;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String [][] mTechLists;

    public static String SerializedSignTransaction = "serializedSignTransaction";

    @Override
    public void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        setContentView(R.layout.activity_nfc);

        loading_view = findViewById(R.id.nfc_loading);
        Glide.with(this).load(R.raw.loading).into(loading_view);

//        Intent intent = getIntent();
//        serializedUnsignedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        Intent nfcIntent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);

        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }

        mFilters = new IntentFilter[]{
                ndef,
        };

        // Setup a tech list for all NFC tags
        mTechLists = new String[][]{
                new String[]{MifareClassic.class.getName()}
        };

        length = -1;

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, mFilters, mTechLists);
        }
    }

    @Override
    protected void onPause() {
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
        super.onPause();
    }


    private void ReadNFC(Intent intent, String action) {
        // Check if it was triggered by a tag discovered interruption
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // Get an instance fo the TAG from the NfcAdapter
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Get an instance of the Mifare classic card from this TAG intent
            MifareClassic tagMfc = MifareClassic.get(tagFromIntent);

            byte[] data;
            try {
                tagMfc.connect();
                int start = 0;

                for (int sectorNum = 0; sectorNum < tagMfc.getSectorCount(); sectorNum++ ){
                    if(tagMfc.authenticateSectorWithKeyA(sectorNum, MifareClassic.KEY_DEFAULT)) {
                        Log.d(TAG, "Authentication success!");

                        for(int blockNum = 0; blockNum < tagMfc.getBlockCountInSector(sectorNum) - 1; blockNum++) {
                            if(start == length) {
                                Log.d(TAG, "Read is complete");
                                break;
                            } else {
                                if (sectorNum == 0 && blockNum == 0) {
                                    continue;
                                }
                                else if(sectorNum == 0 && blockNum == 1){
                                    // Check length of serialized transaction
                                    byte[] record = tagMfc.readBlock(4 * sectorNum + blockNum);
                                    length = byteArrayToInt(Arrays.copyOfRange(record, 0, 4));

                                    // Initialize
                                    serializedSignedTransaction = Arrays.copyOfRange(record, 4, 16);
                                    start += 12;
                                    Log.d(TAG, "Sector " + sectorNum + ", " + "Block "+ blockNum + ": " + toHexString(record));
                                }
                                else {
                                    byte[] record = tagMfc.readBlock(4 * sectorNum + blockNum);
                                    serializedSignedTransaction = concatByteArray(serializedSignedTransaction, record);
                                    start += 16;
                                    Log.d(TAG, "Sector " + sectorNum + ", " + "Block "+ blockNum + ": " + toHexString(record));
                                }
                            }
                        }
                    }
                    else {
                        Log.d(TAG, "Authentication failed!");
                    }

                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
        }
    }

    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        ReadNFC(intent, action);
        getOnlyTransaction();
        Log.d(TAG, "Get Serialized SignTransaction");

        // Go back
        Intent data = new Intent();
        data.putExtra(SerializedSignTransaction, serializedSignedTransaction);
        setResult(CommonStatusCodes.SUCCESS, data);
        finish();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
    }

    private void getOnlyTransaction(){
        serializedSignedTransaction = Arrays.copyOfRange(serializedSignedTransaction, 0, length);
        Log.d(TAG, "Only transaction: " + toHexString(serializedSignedTransaction));
    }



    public static final String CHARS = "0123456789ABCDEF";

    public static String toHexString(byte[] data) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; ++i) {
            sb.append(CHARS.charAt((data[i] >> 4) & 0x0F))
                    .append(CHARS.charAt(data[i] & 0x0F));
        }
        return sb.toString();
    }

    public static byte[] concatByteArray(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public int byteArrayToInt(byte [] b) {
        return (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
    }


}