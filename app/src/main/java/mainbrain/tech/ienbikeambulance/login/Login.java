package mainbrain.tech.ienbikeambulance.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
import java.util.Random;

import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.design.Sansation;

import static mainbrain.tech.ienbikeambulance.R.layout.login;

/**
 * Created by iammike on 16/07/16.
 */
public class Login extends AppCompatActivity
{
    EditText number;
    private String code;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(login);
        //Log.e("start","hi");

        number = EditText.class.cast(findViewById(R.id.editText));
        number.toString();
        if(number.isFocused()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        //??? this line toplayout has height accordin to layout parameters?
        //getResources().getDisplayMetrics().heightPixels this value give you the height of the deveice
        //so I am setting one third  height of the devie to topLayout
        //got it? yes
        //findViewById(R.id.toplayout).getLayoutParams().height this is the method to access a view's height
        findViewById(R.id.toplayout).getLayoutParams().height = getResources().getDisplayMetrics().heightPixels/3;

        // random otp is generated. Since I needed 5 digit code I used like this
        code = String.valueOf(new Random().nextInt(99999 - 11111) + 11111);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calls getPermission method and checks
                if(getPermission()) {
                    if (number.getText().length() != 10) {
                        Toast.makeText(getApplicationContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e("testcode" , code);

                        //here we are calling the asynctask reqMessage and we are passing two values
                        //so the async task do is work in background
                        new reqMessage().execute(number.getText().toString(), code);

                        //at the same time we are calling next acticity
                        //and passing those two values
                        Intent intent = new Intent(Login.this, Verification.class);
                        intent.putExtra("number", number.getText().toString());
                        intent.putExtra("code", code);
                        startActivity(intent);
                        //and closing this activity
                        finish();
                        //this is animation
                        //you can see the xml's in anim folder
                        overridePendingTransition(R.anim.right_in, R.anim.right_out);
                    }
                }
                else
                {

                }
            }
        });
        //this is for the UI part I think
        //this is to change the font. just check with or without it
        //so this is for fonts
        //findViewById(R.id.layout) this is for which you need to change the font
        //you can directly use a edittext or textview
        //or you can mention a view , in this case all the fileds inside this view get changed
        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));

        getPermission();
    }

    public boolean getPermission()
    {
        //permission is granted then send,read sms
        if (ContextCompat.checkSelfPermission(Login.this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(Login.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(Login.this, new String[]{Manifest.permission.RECEIVE_SMS , Manifest.permission.READ_SMS}, 11);

           return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode) {
            // I didn't understand why just case '11'
            case 11: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    //async task is used to run a certain code in backgrounf
    public class reqMessage extends AsyncTask<String, Void, String>
    {
        String results;
        boolean res=true;

        @Override
        protected void onPreExecute()
        {

        }

        //which comes here as String array params
        @Override
        protected String doInBackground(String... params) {
            try {
                //http connection is established
                HttpClient http_client = new DefaultHttpClient();
                //now in server this file will  send a sms to the gicen number with the otp code
                HttpPost http_post = new HttpPost("http://isunapps.com/sales/TaxiApp/Taxi_registration.php");
                //name and code are storedin the array list
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                //so params[0] has number and params[1] has otp code
                nameVP.add(new BasicNameValuePair("number", params[0]));
                nameVP.add(new BasicNameValuePair("code", params[1]));
                //the values are sent to the server?
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                //the values are retrieved in form of an entity
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    entity.consumeContent();
                    //the connection is closed
                    http_client.getConnectionManager().shutdown();
                    //returns success on successful connection
                    results = "Success";
                }
                else
                {
                    results = "Failure";
                }
            }
            catch (Exception e)
            {
                results = e.toString();
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {

        }
    }
}
