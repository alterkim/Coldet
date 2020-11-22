package com.coldblock.coldet.nfc.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.coldblock.coldet.R;
import com.coldblock.coldet.nfc.activity.NfcActivity;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.IOException;
import java.nio.charset.Charset;

public class TagNfcDialog extends Activity {
    private static final String TAG = "TagNfcDialog";

    public static final int REQUEST_NFC_TRANSMISSION = 1020;
    public static final String NFC_ADAPTER = "nfcAdapter";

    private byte[] serializedTransaction;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] mFilters;
    private String [][] mTechLists;

    @Override
    protected void onCreate(Bundle savedBundle) {
        super.onCreate(savedBundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_tag_nfc);

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(params);

        Intent intent = getIntent();
        serializedTransaction = intent.getByteArrayExtra(WalletAddedActivity.Transaction);

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

    }

    private void resolveIntent(Intent intent) {
        // Parse the intent and get the action that triggered this intent
        String action = intent.getAction();
        ReadNFC(intent, action);
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

                for (int i = 0; i < tagMfc.getSectorCount(); i++ ){
                    if(tagMfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_DEFAULT)) {
                        Log.d(TAG, "Authentication success!");

                        for(int j = 0; j < tagMfc.getBlockCountInSector(j); j++) {
                            data = tagMfc.readBlock(4*i +j);
                            Log.d(TAG, toHexString(data));
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NFC_TRANSMISSION) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    System.out.println("Finish NfcActivity");
                }
            }
        }
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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        resolveIntent(intent);
//        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//        if (tag != null) {
//            ProcessNFC();
//        }
    }

    private void ProcessNFC(){
        Intent data = new Intent(TagNfcDialog.this, NfcActivity.class);
        data.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
        startActivityForResult(data, REQUEST_NFC_TRANSMISSION);
    }
    
    private void WriteTransactionToTag() {
        //TODO: Write the transaction data to tag
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

    public static String byteArrayToHexString(byte[] data){
        int length = data.length;
        StringBuilder string_data = new StringBuilder();

        for (byte datum : data) {
            string_data.append(Integer.toHexString((datum >> 4) & 0xf));
            string_data.append(Integer.toHexString(datum & 0xf));
        }

        return string_data.toString();
    }
}
