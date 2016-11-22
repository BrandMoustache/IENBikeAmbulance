package mainbrain.tech.ienbikeambulance.login;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.design.Sansation;
import mainbrain.tech.ienbikeambulance.main.App;
import mainbrain.tech.ienbikeambulance.main.CheckConnection;
import mainbrain.tech.ienbikeambulance.main.Cons;
import mainbrain.tech.ienbikeambulance.main.Home;

/**
 * Created by iammike on 16/07/16.
 */
public class Verification extends AppCompatActivity
{
    MaterialProgressBar progress;
    TextView time;
    TextView changeNumber;
    TextView otpNumber;
    SmsReceiver smsReceiver;
    private CountDownTimer cdt;
    private int total;
    private ProgressDialog progressDialog;

    //yeah. In one place.
    boolean callHandler = true;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verification);
        getSupportActionBar().hide();
        //did you check the SMSReceiver? no
        smsReceiver = new SmsReceiver();

        progress = (MaterialProgressBar) findViewById(R.id.horizontal_progress_library);
        time = TextView.class.cast(findViewById(R.id.textView13));
        changeNumber = TextView.class.cast(findViewById(R.id.textView6));
        otpNumber = TextView.class.cast(findViewById(R.id.textView5));

        otpNumber.setText("Waiting to automatically detect an \nSMS sent to " + getIntent().getStringExtra("number"));
        changeNumber.setText("Not " + getIntent().getStringExtra("number") + "?");

        startTimer();

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeNumber();
            }
        });

        new Sansation().overrideFonts(getApplicationContext(), findViewById(R.id.layout));
    }

    private void changeNumber()
    {
        //this method is called when the user changes his number
        //to avoid that only , we are setting up false to callHandler

        callHandler = false;
        //so when the user click changeNUmber we are saving as false. Next?

        Intent intent = new Intent(Verification.this , Login.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.left_in, R.anim.left_out);
    }

    private void startTimer()
    {
        total = 0;
        Log.e("handler","yes");
        final Handler handler = new Handler();
        final Runnable r = new Runnable()
        {
            public void run()
            {
                total++;
                Log.e("timer1","yes");
                time.setText((60-total)+"s");
                progress.setProgress(total);
                Log.e("timer","yes");

                //so , until the total gets to 60 , the handler will get called for every one sec IF


                //so for 60 sec until callhandler set to false  , the handler will contiue to run.

                //Now , when you click change nuber , the login activity gets displayed
                //but in the backgriund handler is still runnning
                //and when the total reaches 60 ,

                if(total == 60)
                {
                    //if couldn't get or read the message in 60 sec then we will go to this activity to enter the OTP manually
                    //after 60 secs checkOTP class will be called

                    //this will get run
                    Log.e("intent","yes");
                    Intent intent = new Intent(Verification.this , CheckOTP.class);
                    intent.putExtra("number", getIntent().getStringExtra("number"));
                    intent.putExtra("code", getIntent().getStringExtra("code"));
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.right_in, R.anim.right_out);
                }
                else
                {
                    try
                    {
                        //so each sec we are checking that method ,
                        //messsage received - Done
                        String message = smsReceiver.getMessage();
                        //

                        //whether it contains our OTP or not
                        //if the OTP not received
                        //does it contains the same code? - DOne
                        if(message.contains(getIntent().getStringExtra("code")) && callHandler)
                        {
                            //get number from the intent
                            //if the message contains our OTP then we need to ckech whether the user registerd with us or not
                            //is net online - Done
                            if(CheckConnection.isOnline(getApplicationContext())) {
                                //so calling a async task to connect eith server and we passing the number
                                //is this line gets execute?

                                //So defnitely this will get execute - DOne
                                new checkAmbulance().execute(getIntent().getStringExtra("number"));
                                Log.e("chkamb","yes");
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                                handler.postDelayed(this, 1000);
                            }
                        }
                        else
                        {
                            //handler is called again after one sec
                            handler.postDelayed(this, 1000);
                        }
                    }
                    catch(Exception e)
                    {
                        //in which places callHandler state changes?
                        //why I called handler with if conditon? if(true) means it will execute always. callHandler's state changes?
                        //same if there is an exception

                        //callHandler is true.
                        if(callHandler)
                            handler.postDelayed(this, 1000);
                    }
                }
            }
        };

        handler.postDelayed(r, 1000);
    }

    public class checkAmbulance extends AsyncTask<String, Void, String> {
        String results;

        @Override
        protected void onPreExecute() {
            //that is an progressDialog. ok? can you show again sir? ohh okay
            //processing is set as the message

            //it is used mostly for background processes to show users that somthing runnning behind and please wait
//is progress showing?  -done - so there is no error till this point
            progressDialog = new ProgressDialog(Verification.this, AlertDialog.THEME_HOLO_LIGHT);
            progressDialog.setMessage("processing");
            progressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port+"services/checkBike");
//                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/checkAmbulance.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("number", params[0]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    //after sending number to server , server response you which is
                    String response = EntityUtils.toString(entity);
                    //since this line is excuted that means there is no problm with server and you received the
                    //id that you want
                    //so till now the  app works fine - till this line
                    Log.e("test", response + "michael");
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();

                    //which is the response from the server which is the bike id
                    results = response;
                    //and we passing that response to onPosetExecute
                } else {
                    results = "Failure";
                }
            } catch (Exception e) {
                results = e.toString();
                Log.e("server", results);
            }
            //and we returning the result
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {
            //now the variable result contains the response from the server

            //the return value is now the result
            progressDialog.cancel();
            //if cancel is pressed then not registered message is displayed?

            //if the response is 0 , then it means there is no user registerd with this number
            //so now the conditon
            //is the result variable is 0? No.- so conditon false
            if (result.equals("0"))
            {
                //so we are shoing a dialo to register first
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Verification.this);
                alertDialogBuilder.setTitle("Not Registered");
                alertDialogBuilder
                        .setMessage("Not registerd as ambulance. Please contact us on 9443153157 for registration.")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id)
                            {
                                finish();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            // the fir_id_ is retrieved from the result and the phone number and id are committed meaning made permanent
            //if the respinse is not 0 and contains fir_id_ , then it is registerd in the server
            //now next conditon , is it contains fir_id? still no. so again false
            //so you can easily find that since we receiving the id of bike which never contains fir_id the problm with thi line
            //so we changed it.........
            else if(result.contains("bik_id_"))
            {
                //so we making loogedin as true
                App.editor.putBoolean("loggedin", true).commit();
                //calling home activity
                Intent intent = new Intent(Verification.this, Home.class);
                //saving the nunber and the id in sharedoreference
                App.editor.putString("number", getIntent().getStringExtra("number")).commit();
                App.editor.putString("id", result).commit();
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
            else
            {
                    //so atlast the code comes here.
                //this is where the code stops// STOPSHIP: 21-11-2016
                //since you dont have any code here , the app stops working.. whcih u mentioned as stucks.
            }
        }
    }
}
