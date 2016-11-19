package mainbrain.tech.ienbikeambulance.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by iammike on 18/07/16.
 */

//this is an broadcast receiver
public class SmsReceiver extends BroadcastReceiver {

    private static String full_text_message=null;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        //so if any msg come to your phone , this method will get run
        //and the datas will be in intent
        Log.e("receiver" , "A Message Received");
        try {

            //if you want go through this code block
            //but the use is  to get the message received in your mobile
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String str = "";
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];

                //this msgs will have your message in array

                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    //str += "SMS from" + msgs[i].getOriginatingAddress();
                    //str += ":";
                    //we are getting a string from that array
                    str += msgs[i].getMessageBody().toString();
                    //str += "\n";
                }

                //Toast.makeText(context, str, Toast.LENGTH_SHORT).show();

                //checking whether it is the message we needed or not.
                //if the msg contains the string IRN , then it is our otp message/
                if (str.contains("IEN")) {
                    //if it the messaege
                    full_text_message = str;
                }
            }
        }
        catch(Exception e)
        {
            Log.e("test" , e.toString()+"cgh");
        }
    }

//then we can get that by using this method
    public String getMessage(){
        return full_text_message;
    }



}