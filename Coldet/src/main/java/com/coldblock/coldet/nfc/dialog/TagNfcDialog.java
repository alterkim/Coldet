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
import com.coldblock.coldet.nfc.service.myHostApduService;
import com.coldblock.coldet.wallet.activity.WalletAddedActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

public class TagNfcDialog extends Activity {
    private static final String TAG = "TagNfcDialog";

    public static final int REQUEST_NFC_TRANSMISSION = 1020;
    public static final int MAX_TAG_VOLUME = 752;
    public static final int MAX_RECORD_VOLUME = 16;
    public static final String NFC_ADAPTER = "nfcAdapter";

    private byte[] serializedTransaction;
    private byte[] length;

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
        // Add length information
        length = intToByteArray(serializedTransaction.length);
        if(serializedTransaction.length % MAX_RECORD_VOLUME != 0) {
            int needByteForFullRecord = MAX_RECORD_VOLUME - ((serializedTransaction.length + 4) % MAX_RECORD_VOLUME);
            byte[] additional = CreateByteArrayForMaxRecord(needByteForFullRecord);
            serializedTransaction =concatByteArray(serializedTransaction, additional);
        }

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
//        Intent apduIntent = new Intent(TagNfcDialog.this, myHostApduService.class);
//        apduIntent.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
//        startService(apduIntent);
    }

    private void resolveIntent(Intent intent) {
        // Parse the intent and get the action that triggered this intent
        String action = intent.getAction();
        //ReadNFC(intent, action);
        WriteNFC(intent, action);

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

                for (int sectorNum = 0; sectorNum < tagMfc.getSectorCount(); sectorNum++ ){
                    if(tagMfc.authenticateSectorWithKeyA(sectorNum, MifareClassic.KEY_DEFAULT)) {
                        Log.d(TAG, "Authentication success!");

                        for(int blockNum = 0; blockNum < tagMfc.getBlockCountInSector(sectorNum); blockNum++) {
                            data = tagMfc.readBlock(4*sectorNum +blockNum);
                            Log.d(TAG, "Sector " + sectorNum + ", " + "Block "+ blockNum + ": " + toHexString(data));
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

    private void WriteNFC(Intent intent, String action){
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {
            // Get an instance fo the TAG from the NfcAdapter
            Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Get an instance of the Mifare classic card from this TAG intent
            MifareClassic tagMfc = MifareClassic.get(tagFromIntent);

            try {
                tagMfc.connect();

                if(serializedTransaction.length > MAX_TAG_VOLUME) {
                    Log.d(TAG, "The size of transaction is bigger than available size");
                } else {
                    int start = 0;
                    for (int sectorNum = 0; sectorNum < tagMfc.getSectorCount(); sectorNum++) {
                        if (tagMfc.authenticateSectorWithKeyA(sectorNum, MifareClassic.KEY_DEFAULT)) {
                            Log.d(TAG, "Authentication success!");

                            for (int blockNum = 0; blockNum < tagMfc.getBlockCountInSector(blockNum) - 1; blockNum++) {
                                if (start == serializedTransaction.length) {
                                    Log.d(TAG, "Write is complete");
                                    break;
                                } else {
                                    if (sectorNum == 0 && blockNum == 0){
                                        continue;
                                    }
                                    else if (sectorNum == 0 && blockNum == 1) {
                                        byte[] record = Arrays.copyOfRange(serializedTransaction, start, start + 12);
                                        record = concatByteArray(length, record);
                                        tagMfc.writeBlock(4 * sectorNum + blockNum, record);
                                        Log.d(TAG, "Sector " + sectorNum + ", " + "Block "+ blockNum + ": " + toHexString(record));
                                        start += 12;
                                    }
                                    else {
                                        byte[] record = Arrays.copyOfRange(serializedTransaction, start, start + 16);
                                        tagMfc.writeBlock(4 * sectorNum + blockNum, record);
                                        Log.d(TAG, "Sector " + sectorNum + ", " + "Block "+ blockNum + ": " + toHexString(record));
                                        start += 16;
                                    }
                                }
                            }
                        } else {
                            Log.d(TAG, "Authentication failed!");
                        }
                    }
                }

            } catch (IOException e) {
                Log.d(TAG, e.getLocalizedMessage());
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
        Log.d(TAG, "Complete writing");
        try {
            Log.d(TAG, "Turn off the NFC");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!nfcAdapter.isEnabled()){
            ProcessNFC();
        }
    }

    private void ProcessNFC(){
        Intent data = new Intent(TagNfcDialog.this, NfcActivity.class);
        data.putExtra(WalletAddedActivity.Transaction, serializedTransaction);
        startActivityForResult(data, REQUEST_NFC_TRANSMISSION);
    }

    private byte[] CreateByteArrayForMaxRecord(int needByteForFullRecord) {
        StringBuilder string_data = new StringBuilder();
        for (int i = 0; i < needByteForFullRecord; i++ ){
            string_data.append("ff");
        }
        return hexStringToByteArray(string_data.toString());
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

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static byte[] concatByteArray(final byte[] array1, byte[] array2) {
        byte[] joinedArray = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static byte[] intToByteArray(int length) {
        byte[] bytes=new byte[4];
        bytes[0]=(byte)((length & 0xFF000000)>>24);
        bytes[1]=(byte)((length & 0x00FF0000)>>16);
        bytes[2]=(byte)((length & 0x0000FF00)>>8);
        bytes[3]=(byte) (length & 0x000000FF);


        return bytes;
    }
}
