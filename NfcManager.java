package com.farhanahmed.nfclibrary;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Parcelable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by farhanahmed on 06/08/2016.
 */
public class NfcManager {
    private static final String TAG = NfcManager.class.getSimpleName();
    private Activity activity;
    private NfcAdapter nfcAdapter;
    private boolean debug = false;
    private Tag tag;
    private Intent nfcIntent;
    public NfcManager(Activity activity) {
        this.activity = activity;
        nfcAdapter = NfcAdapter.getDefaultAdapter(activity);
    }

    public void setDebug(boolean debug){
        this.debug = debug;

    }
    /*call this method on activity onResume method*/
    public void enableForegroundDispatch(Class< ? extends Activity> activityClass){

        Intent intent = new Intent(activity,activityClass);
        intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity,0,intent,0);
        IntentFilter[] intentFilters = new IntentFilter[]{};
        nfcAdapter.enableForegroundDispatch(activity,pendingIntent,intentFilters,null);
    }
    /*call this method on activity onPause method*/
    public void disableForegroundDispatch(){
        nfcAdapter.disableForegroundDispatch(activity);
    }
    public void formatTag(Tag tag, NdefMessage message)
    {
        try
        {
            NdefFormatable formatable = NdefFormatable.get(tag);
            
            if (formatable == null)
            {
                logError("Tag not Format able");
            }else {
                formatable.connect();
                formatable.format(message);
                formatable.close();
                logDebug("Tag content written");
            }

        }catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void writeNDefMessage(Tag tag,NdefMessage message){
        try{
            if (tag == null)
            {
                logError("cannot write on null tag");
                return;
            }
            Ndef ndef = Ndef.get(tag);
            if(ndef == null)
            {
                formatTag(tag,message);
            }else {
                ndef.connect();
                if (!ndef.isWritable())
                {
                   logError("Tag not writable");
                    ndef.close();
                    return;
                }

                ndef.writeNdefMessage(message);
               logDebug("Tag content written");
                ndef.close();
            }
        }catch (Exception e)
        {
            Log.e("writeNDefMessage",e.getMessage());
            e.printStackTrace();
        }
    }
    /*create ndef message record; created from text/plain content*/
    private NdefRecord createTextRecord(String data){
        try {
            final byte[] text = data.getBytes("utf-8");
            final int textSize = text.length;
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(textSize);
            outputStream.write(text,0,textSize);
            return  new NdefRecord(NdefRecord.TNF_WELL_KNOWN,NdefRecord.RTD_TEXT,new byte[0],outputStream.toByteArray());
        }catch (UnsupportedEncodingException e) {
            Log.e("createTextRecord",e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    /*create ndef message container, store record; created from text/plain content*/
    private NdefMessage createNDefMessage(String data){
        NdefRecord ndefRecord = createTextRecord(data);
        NdefMessage message = new NdefMessage(new NdefRecord[]{ndefRecord});
        return message;
    }

    private void logError(String message) {
        if (debug) {
            Log.e(TAG, message);
        }
    }
    /*In debug mode it will show logs in logcat(Android Monitor)*/
    private void logDebug(String message){
        if (debug)
        {
            Log.d(TAG,message);
        }
    }
    /*read data from ndef record container *(NdefMessage)*/
    private String readDataFromNDefMessage(NdefMessage message) {

        if (message != null)
        {
            NdefRecord [] records = message.getRecords();

            if (records.length>0)
            {
                return readDataFromNDefRecord(records[0]);
            }
        }
        return null;
    }
    /*get byte array and convert it into String*/
    private String readDataFromNDefRecord(NdefRecord record) {

        final byte[] payLoad = record.getPayload();
        return new String(payLoad);
    }
    /*Call this method on activity 'onNewIntent' method*/
    public void onNewIntent(Intent intent){
        if (intent!= null && (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) )
        {
            nfcIntent = intent;
            logDebug("New Action "+intent.getAction());
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }else {
           logError("Intent action not match");
        }
    }
    /*read text content from tag, if found empty this returns empty string*/
    public String readContent() {
        if (tag == null)
        {
            logError("setReadListener : Tag is null");
            return null;
        }
        if (nfcIntent == null)
        {
            logError("setReadListener : NFC Intent is null");
            return null;
        }

        Parcelable[] parcelables = nfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if(parcelables == null)
        {
            return "";
        }
        if (parcelables.length>0)
        {
            String data = readDataFromNDefMessage((NdefMessage)parcelables[0]);
            logDebug(data);
            return data;
        }
        return null;
    }
    /*write text content on tag*/
    public void writeContent(String content){
        if (tag!= null)
        {
            NdefMessage message = createNDefMessage(content);
            writeNDefMessage(tag,message);
        }
    }
    /*release activity instance to avoid memory leaks*/
    public void release()
    {
        activity =null;
    }
}
