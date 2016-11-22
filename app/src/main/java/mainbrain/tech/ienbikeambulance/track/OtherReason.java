package mainbrain.tech.ienbikeambulance.track;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

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
import mainbrain.tech.ienbikeambulance.main.App;
import mainbrain.tech.ienbikeambulance.main.Cons;

/**
 * Created by iammike on 25/07/16.
 */
public class OtherReason extends AppCompatActivity
{
    EditText reason;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.otherreason);


        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));

        reason = (EditText) findViewById(R.id.editText);
        reason.setText(" ");
        reason.setTextIsSelectable(true);

        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new addReason().execute(App.shared.getString("id" , "") , App.shared.getString("sosid" , "") , reason.getText().toString());
            }
        });
    }

    public class addReason extends AsyncTask<String, Void, String>
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
                HttpPost http_post = new HttpPost(Cons.port+"sos/addReason");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("ersid" , params[0]));
                nameVP.add(new BasicNameValuePair("sosid" , params[1]));
                nameVP.add(new BasicNameValuePair("reason" , params[2]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test" , response);
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
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {
            if(result.equals("1"))
            {
                finish();
            }
        }
    }
}
