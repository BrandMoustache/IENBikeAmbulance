package mainbrain.tech.ienbikeambulance.main;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.DisplayMetrics;


import mainbrain.tech.ienbikeambulance.ImageLoader.UILApplication;

public class App extends Application
{

    public static final int HOME = 10;
    public static final int HOME_REQUEST = 11;

    public static final int SERVICEREQUEST = 20;
    public static final int SERVICEREQUEST_ACCEPT = 21;
    public static final int SERVICEREQUEST_REJECT = 22;

    public static final int TRACK = 30;
    public static final int END_EMERGENCY = 31;
    public static final int FALSE_ALARM = 32;
    public static final int FORWARD_SOS = 33;

    public static final int WAY_TO_HOSPITAL = 41;
    public static final int CALL_OTHER_SERVICE = 42;

    public static final int FCM = 50;

    public static int notificationID = 1;

    public static String ECONTACTS = "econtacts";
    public static String EADDRESS = "eAddress";

    public static SharedPreferences shared;
    public static SharedPreferences.Editor editor;
    public static int swidth;
    public static int sheight;

    @Override
    public void onCreate() {
        super.onCreate();

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        swidth = displayMetrics.widthPixels;
        sheight = displayMetrics.heightPixels;

        shared=getSharedPreferences("ienfiresrsservice", MODE_PRIVATE);
        editor=shared.edit();


        UILApplication.initImageLoader(getApplicationContext());
    }
}
