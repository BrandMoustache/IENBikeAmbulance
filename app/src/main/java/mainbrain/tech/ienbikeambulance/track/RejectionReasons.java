package mainbrain.tech.ienbikeambulance.track;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
public class RejectionReasons extends AppCompatActivity
{
    private ArrayAdapter<String> arrayAdapter;
    ListView listView;

    int selected = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.rejectreasons);


        listView = (ListView) findViewById(R.id.listView);

        new Sansation().overrideFonts(getApplicationContext() , findViewById(R.id.layout));

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext() , android.R.layout.select_dialog_item)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View view = super.getView(position, convertView, parent);
                TextView text1 = (TextView) view.findViewById(android.R.id.text1);
                text1.setGravity(Gravity.CENTER);
                text1.setTextSize(15);
                new Sansation().overrideFonts(getApplicationContext(), text1);
                if(selected == position)
                {
                    text1.setTextColor(Color.WHITE);
                    text1.setBackgroundColor(Color.parseColor("#58a3da"));
                }
                else
                {
                    text1.setTextColor(Color.parseColor("#212121"));
                    text1.setBackgroundColor(Color.WHITE);
                }
                return view;
            }
        };

        arrayAdapter.add("Reason 1");
        arrayAdapter.add("Reason 2");
        arrayAdapter.add("Reason 3");
        arrayAdapter.add("Reason 4");
        arrayAdapter.add("Other Reason");

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==arrayAdapter.getCount()-1)
                {
                    Intent intent = new Intent(RejectionReasons.this , OtherReason.class);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    selected = i;
                    arrayAdapter.notifyDataSetChanged();
                }
            }
        });

        listView.setAdapter(arrayAdapter);

        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new addReason().execute(App.shared.getString("id" , "") , App.shared.getString("sosid" , "") , arrayAdapter.getItem(selected).toString());
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
