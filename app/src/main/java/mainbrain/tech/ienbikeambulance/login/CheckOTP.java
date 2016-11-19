package mainbrain.tech.ienbikeambulance.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.design.CustomProgressDialog;
import mainbrain.tech.ienbikeambulance.design.Sansation;
import mainbrain.tech.ienbikeambulance.main.App;
import mainbrain.tech.ienbikeambulance.main.Cons;
import mainbrain.tech.ienbikeambulance.main.Home;

/**
 * Created by iammike on 16/07/16.
 */
public class CheckOTP extends Activity
{
    private CustomProgressDialog progressDialog;
    TextView txtv1,txtv2,txtv3,txtv4,txtv5,changenum;
    EditText edt_txtotp;
    private static final String LETTER_SPACING = "  ";
    private String myPreviousText;
    LinearLayout ll_id;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getActionBar().hide();
        setContentView(R.layout.checkotp);


        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));
        txtv1=(TextView)findViewById(R.id.txtv_line1);
        txtv2=(TextView)findViewById(R.id.txtv_line2);
        txtv3=(TextView)findViewById(R.id.txtv_line3);
        txtv4=(TextView)findViewById(R.id.txtv_line4);
        txtv5=(TextView)findViewById(R.id.txtv_line5);
        changenum=(TextView)findViewById(R.id.textView6);
        edt_txtotp=(EditText)findViewById(R.id.edttxt_otp);
        ll_id=(LinearLayout)findViewById(R.id.ll_id);
        intent = getIntent();
        changenum.setText("Not " + getIntent().getStringExtra("number") + "?");

        //leave this. will come to it later
        edt_txtotp.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int x, int i1, int i2)
            {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }
            @Override
            public void afterTextChanged(Editable editable)
            {
                String edttxt_otp = editable.toString();
                StringBuilder newText = new StringBuilder();
                // Only update the EditText when the user modify it -> Otherwise it will be triggered when adding spaces
                if (!edttxt_otp.equals(myPreviousText))
                {
                    // Remove spaces
                    edttxt_otp = edttxt_otp.replace(" ", "");
                    // Add space between each character

                    for (int i = 0; i < edttxt_otp.length(); i++)
                    {
                        if (i == edttxt_otp.length() - 1)
                        {
                            // Do not add a space after the last character -> Allow user to delete last character
                            newText.append(Character.toUpperCase(edttxt_otp.charAt(edttxt_otp.length() - 1)));

                        }
                        else
                        {
                            newText.append(Character.toUpperCase(edttxt_otp.charAt(i)) +LETTER_SPACING);
                        }
                    }
                    myPreviousText = newText.toString();
                    // Update the text with spaces and place the cursor at the end
                    edt_txtotp.setText(newText);
                    edt_txtotp.setSelection(newText.length());

                }
                if(newText.length()==1)
                {
                    txtv1.setVisibility(View.GONE);
                    txtv2.setVisibility(View.VISIBLE);
                    txtv3.setVisibility(View.VISIBLE);
                    txtv4.setVisibility(View.VISIBLE);
                    txtv5.setVisibility(View.VISIBLE);
                    ll_id.setVisibility(View.VISIBLE);


                }else if(newText.length()==4)
                {
                    txtv1.setVisibility(View.GONE);
                    txtv2.setVisibility(View.GONE);
                    txtv3.setVisibility(View.VISIBLE);
                    txtv4.setVisibility(View.VISIBLE);
                    ll_id.setVisibility(View.VISIBLE);
                    txtv5.setVisibility(View.VISIBLE);

                }else if(newText.length()==7)
                {
                    txtv1.setVisibility(View.GONE);
                    txtv2.setVisibility(View.GONE);
                    txtv3.setVisibility(View.GONE);
                    txtv4.setVisibility(View.VISIBLE);
                    txtv5.setVisibility(View.VISIBLE);
                    ll_id.setVisibility(View.VISIBLE);

                }else if(edttxt_otp.length()==10)
                {
                    txtv1.setVisibility(View.GONE);
                    txtv2.setVisibility(View.GONE);
                    txtv3.setVisibility(View.GONE);
                    txtv4.setVisibility(View.GONE);
                    ll_id.setVisibility(View.VISIBLE);
                    txtv5.setVisibility(View.VISIBLE);

                }else if(edttxt_otp.length()==13)
                {
                    txtv1.setVisibility(View.GONE);
                    txtv2.setVisibility(View.GONE);
                    txtv3.setVisibility(View.GONE);
                    txtv4.setVisibility(View.GONE);
                    ll_id.setVisibility(View.GONE);
                    txtv5.setVisibility(View.GONE);

                }
                else if(edttxt_otp.length()==0)
                {
                    txtv1.setVisibility(View.VISIBLE);
                    txtv2.setVisibility(View.VISIBLE);
                    txtv3.setVisibility(View.VISIBLE);
                    txtv4.setVisibility(View.VISIBLE);
                    ll_id.setVisibility(View.VISIBLE);
                    txtv5.setVisibility(View.VISIBLE);
                }

            }
        });

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CheckOTP.this , Login.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.left_in, R.anim.left_out);
            }
        });
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strcode=getIntent().getStringExtra("code");
                try
                {
                    if(edt_txtotp.getText().toString().length()==0)
                    {
                        Toast.makeText(CheckOTP.this, getResources().getString(R.string.Otpvalidation), Toast.LENGTH_SHORT).show();
                    }else {
                        if (strcode == null || strcode.length() == 0 || strcode.isEmpty()) {
                            Toast.makeText(CheckOTP.this, getResources().getString(R.string.Otpnull), Toast.LENGTH_SHORT).show();
                        } else {
                            //check otp
                            if (getIntent().getStringExtra("code").equals(edt_txtotp.getText().toString().replace(" ", ""))) {
                                new checkAmbulance().execute(getIntent().getStringExtra("number"));
                            }
                        }
                    }
                }catch(Exception e)
                {
                    e.printStackTrace();
                }


            }
        });

        findViewById(R.id.textView8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new reqMessage().execute(getIntent().getStringExtra("number") , getIntent().getStringExtra("code"));
            }
        });

        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));
    }

    public class reqMessage extends AsyncTask<String, Void, String>
    {
        String results;
        boolean res=true;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost("http://isunapps.com/sales/TaxiApp/Taxi_registration.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("number", params[0]));
                nameVP.add(new BasicNameValuePair("code", params[1]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test", response);
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();
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

    public class checkAmbulance extends AsyncTask<String, Void, String> {
        String results;

        @Override
        protected void onPreExecute() {
            progressDialog = new CustomProgressDialog(CheckOTP.this, AlertDialog.THEME_HOLO_LIGHT);
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
                    String response = EntityUtils.toString(entity);
                    Log.e("test", response + "michael");
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();

                    results = response;
                } else {
                    results = "Failure";
                }
            } catch (Exception e) {
                results = e.toString();
                Log.e("server", results);
            }
            Log.e("test","home");
            return results;

        }

        @Override
        protected void onPostExecute(final String result)
        {
            Log.e("test" , "postwillexcuteafterdoinbackground");
            progressDialog.cancel();
            if (result.equals("0"))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckOTP.this);
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
            //Log.e("test","before elseif");
            else if(result.contains("bik_id_"))
            {
                Log.e("test2","home");
                App.editor.putBoolean("loggedin", true).commit();
                Intent intent = new Intent(CheckOTP.this, Home.class);
                Log.e("test3","home");
                App.editor.putString("number", getIntent().getStringExtra("number")).commit();
                App.editor.putString("id", result).commit();
                Log.e("test4","home");
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
            else
            {

            }
        }
    }
}
