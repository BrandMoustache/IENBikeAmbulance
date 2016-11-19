package mainbrain.tech.ienbikeambulance.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.login.Login;


/**
 * Created by iammike on 08/09/16.
 */

public class LandingPage extends AppCompatActivity
{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landingpage);
        getActionBar().hide();

        //return to Login.class page if the user not logged in

        //yeah yeah not()

        //understand this class? yes except !app line

        // what value this line return if the user opens the app for the first time true?
        //no
        // App.shared.getBoolean("loggedin" , false)

        //here the false is the default value. Since we didnt set any value for loggedin key it will return the default value
        //got it?
        //so you understand thie class? yes
        //can you explain this class line by line
        if(!App.shared.getBoolean("loggedin" , false))
        {
            startActivity(new Intent(LandingPage.this , Login.class));
            finish();
            return;
        }

        else
        {
            startActivity(new Intent(LandingPage.this , Home.class));
            finish();

        }
    }
}
