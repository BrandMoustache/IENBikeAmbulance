package mainbrain.tech.ienbikeambulance.track;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

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

import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.design.Sansation;
import mainbrain.tech.ienbikeambulance.location.Constants;
import mainbrain.tech.ienbikeambulance.location.FetchAddressIntentService;
import mainbrain.tech.ienbikeambulance.main.App;
import mainbrain.tech.ienbikeambulance.main.Cons;
import mainbrain.tech.ienbikeambulance.main.Home;
import mainbrain.tech.ienbikeambulance.main.MySmackService;

/**
 * Created by iammike on 25/07/16.
 */
public class ServiceRequest extends AppCompatActivity implements ServiceConnection
{
    private Messenger mServiceMessenger = null;
    boolean mIsBound;
    private static final String LOGTAG = "MainActivity";
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;

    private AddressResultReceiver mResultReceiver;
    protected String mAddressOutput;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.servicerequest);
        getActionBar().hide();
        automaticBind();

        mResultReceiver = new AddressResultReceiver(new Handler());

        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));

        final CountDownTimer cdt = getCountDownTimer();

        findViewById(R.id.textView3).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                cdt.cancel();
                new checkstatus().execute(getIntent().getStringExtra("sosid"));
            }
        });

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                cdt.cancel();
                sendReject(getIntent().getStringExtra("userid") , getIntent().getStringExtra("sosid"));
                Intent intent = new Intent(ServiceRequest.this , RejectionReasons.class);
                intent.putExtra("sosid" , getIntent().getStringExtra("sosid"));
                startActivity(intent);
                finish();
            }
        });

        startIntentService(new LatLng(getIntent().getDoubleExtra("latitude", 0.0) , getIntent().getDoubleExtra("longitude", 0.0)));
    }

    private CountDownTimer getCountDownTimer()
    {
        final int oneMin= 1 * 60 * 1000;

        return new CountDownTimer(oneMin, 1000) {

            int i=0;
            public void onTick(long millisUntilFinished)
            {
                if(60-i<10)
                {
                    TextView.class.cast(findViewById(R.id.textView4)).setText("0"+String.valueOf((60-i)));
                }
                else
                {
                    TextView.class.cast(findViewById(R.id.textView4)).setText(String.valueOf((60-i)));
                }
                i++;
            }

            public void onFinish()
            {
                sendReject(getIntent().getStringExtra("userid") , getIntent().getStringExtra("sosid"));
                Intent intent = new Intent(ServiceRequest.this , RejectionReasons.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }

//Code to connect with smackservice

    private void automaticBind()
    {
        doBindService();
    }

    private void doBindService()
    {
        bindService(new Intent(this, MySmackService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        sendMessageToService(0);
    }

    private void doUnbindService()
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    android.os.Message msg = android.os.Message.obtain(null, MySmackService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                }
                catch (RemoteException e)
                {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        mServiceMessenger = new Messenger(service);
        try
        {
            android.os.Message msg = android.os.Message.obtain(null, MySmackService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        }
        catch (RemoteException e)
        {
            // In this case the service has crashed before we could even do anything with it
        }
        sendMessageToService(0);
    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        mServiceMessenger = null;
    }

    private class IncomingMessageHandler extends Handler
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case MySmackService.MSG_SET_INT_VALUE:
                    break;

                case MySmackService.MSG_SET_STRING_VALUE:
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    private void sendMessageToService(int intvaluetosend)
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    android.os.Message msg = android.os.Message.obtain(null, App.SERVICEREQUEST, intvaluetosend, 0);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                }
                catch (RemoteException e)
                {
                    Log.e("BIND", e.toString());
                }
            }
        }
    }

    private void sendAccept(String to , String id)
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    // Send data as a String
                    Bundle bundle = new Bundle();
                    bundle.putString("to", to);
                    bundle.putString("id", id);
                    android.os.Message msg = android.os.Message.obtain(null, App.SERVICEREQUEST_ACCEPT);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }

            }
        }
    }

    private void sendReject(String to , String id)
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    // Send data as a String
                    Bundle bundle = new Bundle();
                    bundle.putString("to", to);
                    bundle.putString("id", id);
                    android.os.Message msg = android.os.Message.obtain(null, App.SERVICEREQUEST_REJECT);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }

            }
        }
    }

    private void sendAcceptFromAnotherService(String to , String id)
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    // Send data as a String
                    Bundle bundle = new Bundle();
                    bundle.putString("to", to);
                    bundle.putString("id", id);
                    android.os.Message msg = android.os.Message.obtain(null, 111);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }

            }
        }
    }

    private void sendRejectFromAnotherService(String to , String id)
    {
        if (mIsBound)
        {
            if (mServiceMessenger != null)
            {
                try
                {
                    // Send data as a String
                    Bundle bundle = new Bundle();
                    bundle.putString("to", to);
                    bundle.putString("id", id);
                    android.os.Message msg = android.os.Message.obtain(null, 121);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                }
                catch (Exception e)
                {
                    System.out.println(e.toString());
                }

            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendMessageToService(0);
    }

    @Override
    public void onStop()
    {
        sendMessageToService(1);
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        try
        {
            sendMessageToService(1);
            doUnbindService();
        }
        catch (Throwable t)
        {
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService(LatLng mLastLocation)
    {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.e("test" , mAddressOutput);
            TextView.class.cast(findViewById(R.id.textView2)).setText(mAddressOutput);
        }
    }

    public class checkstatus extends AsyncTask<String, Void, String>
    {
        String results;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port+"sos/getsosstatus");
//                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/checkstatus.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("service", "ambulance_id"));
                nameVP.add(new BasicNameValuePair("sosid", params[0]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null)
                {
                    String response = EntityUtils.toString(entity);
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();

                    results = response;
                }
                else
                {
                    results = "Failure";
                }
            }
            catch (Exception e)
            {
                results = e.toString();
                Log.e("server", results);
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {
            if(result.equals("0"))
            {
//                if (App.shared.getString("sosid", "").contains("&")) {
//                    String subject = App.shared.getString("service_id", "");
//                    String senderid = subject.substring(subject.indexOf("&"), subject.length());
//                    sendAccept(App.shared.getString("userid", ""), App.shared.getString("service_id", "").substring(0, App.shared.getString("service_id", "").indexOf("&")));
//                    sendAcceptFromAnotherService(senderid, App.shared.getString("service_id", ""));
//                } else {
//                    sendAccept(App.shared.getString("userid", ""), App.shared.getString("sosid", ""));
//                }

                App.editor.putString("userid" , getIntent().getStringExtra("userid")).commit();
                App.editor.putString("sosid" , getIntent().getStringExtra("sosid")).commit();

                sendAccept(App.shared.getString("userid", ""), App.shared.getString("sosid", ""));

                Intent intent = new Intent(ServiceRequest.this, Track.class);
                intent.putExtra("subject", "SOS");
                intent.putExtra("self", getIntent().getStringExtra("self"));
                intent.putExtra("latitude", getIntent().getDoubleExtra("latitude", 0.0));
                intent.putExtra("longitude", getIntent().getDoubleExtra("longitude", 0.0));
                startActivity(intent);
                finish();
            }
            else if(result.equals("1"))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        ServiceRequest.this);

                // set title
                alertDialogBuilder.setTitle("SOS Response");

                // set dialog message
                alertDialogBuilder
                        .setMessage("This sos request is invalid now.")
                        .setCancelable(false)
                        .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id)
                            {
                                startActivity(new Intent(ServiceRequest.this , Home.class));
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
            else
            {

            }
        }
    }
}
