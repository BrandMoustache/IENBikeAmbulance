package mainbrain.tech.ienbikeambulance.track;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.design.Sansation;
import mainbrain.tech.ienbikeambulance.main.App;
import mainbrain.tech.ienbikeambulance.main.Cons;
import mainbrain.tech.ienbikeambulance.main.Home;
import mainbrain.tech.ienbikeambulance.main.MySmackService;

//this activity will show when the request is accepted

public class Track extends AppCompatActivity implements OnMapReadyCallback,
        ServiceConnection, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    //this block of code related to map and direction
    private GoogleMap mMap;
    private PolylineOptions polyline;
    private MarkerOptions markerOptions1;
    private MarkerOptions markerOptions2;
    private ArrayList<LatLng> directionPositionList;
    private Polyline myPolyline;
    private Marker marker1;
    private Marker marker2;

    //we already discussed this
    private Messenger mServiceMessenger = null;
    final Handler handler = new Handler();
    boolean mIsBound;
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;
    //

    private LatLng userLocation;
    Animation animovetextbottamtotop, animovetexttoptobottam;

    String name, gender, dob;
    TextView nameage, gen;
    TextView progressText;

    boolean clearTraffic = false;

    //this is related to getting location that we discussed
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    double userLatitude, userLongitude;

    //the variables in grey color means it is not used in the application. You dont need to worry abt that
    private String medical;
    private PopupWindow popupWindow;
        //??
    String[] keys = {"bloodgroup" , "allergies" , "illness" , "medication" , "hospitalname" ,
            "hospitalid" , "insurancename" , "policyno" , "physicanname" , "physicannumber" };

    String[] values = new String[10];
    private AlertDialog alertDialog;


    private static final String GOOGLE_API_KEY = "AIzaSyCrrDBseI3PiESGECpeerPTVBzB2rtN-Oc";
    ArrayList<Service> services = new ArrayList();
    ArrayList<String> _id = new ArrayList();
    private String ambulance_number = "null";
    private boolean reachedUser = false;

    ImageView contactAmbulance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track);
        getActionBar().hide();

        //it has the id imageView5
        //so you found the button in the design
        //as you can see it is to call the ambulance
        contactAmbulance = ImageView.class.cast(findViewById(R.id.imageView5));

        if (getIntent().getStringExtra("subject").equals("SOSES")) {
            endEmergency();
        } else if (getIntent().getStringExtra("subject").equals("SOSFA")) {
            falseAlarm();
        } else {
            new Sansation().overrideFonts(getApplicationContext(), findViewById(R.id.layout));

            automaticBind();

            buildGoogleApiClient();
            Log.e("get","longlat");
            userLatitude = getIntent().getDoubleExtra("latitude", 0.0);
            userLongitude = getIntent().getDoubleExtra("longitude", 0.0);

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.e("loca","updated");
            mLocationRequest.setInterval(10000); // Update location every second

            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.location_map);
            mapFragment.getMapAsync(this);

            nameage = TextView.class.cast(findViewById(R.id.textView11));
            gen = TextView.class.cast(findViewById(R.id.textView12));
            progressText = TextView.class.cast(findViewById(R.id.textView20));

            animovetextbottamtotop = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.movetextviewbottamtotop);
            animovetexttoptobottam = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.movetextviewtoptobottam);

            //show contact details
            findViewById(R.id.rl_contactdetails).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findViewById(R.id.ll_contactdetails).startAnimation(animovetextbottamtotop);
                    findViewById(R.id.rl_contactdetails).setVisibility(View.GONE);
                    findViewById(R.id.ll_contactdetails).setVisibility(View.VISIBLE);
                }
            });

            //hide contact deytails
            findViewById(R.id.txtv_contact).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findViewById(R.id.ll_contactdetails).startAnimation(animovetexttoptobottam);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.ll_contactdetails).clearAnimation();
                            findViewById(R.id.ll_contactdetails).setVisibility(View.GONE);
                            findViewById(R.id.rl_contactdetails).setVisibility(View.VISIBLE);
                        }
                    }, 600);


                }
            });

            //Sending request to traffic polices
            findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "Sending traffic clearence Request", Toast.LENGTH_SHORT).show();
                    clearTraffic = true;
                }
            });

            if (getIntent().getStringExtra("self").equals("1")) {
                new getDetails().execute(App.shared.getString("userid", ""));
            }

            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .considerExifParams(true)
                    .build();

            CircleImageView circleImageView = CircleImageView.class.cast(findViewById(R.id.imageView3));

            ImageLoader.getInstance().displayImage("http://brandmoustache.com/ClubApp/images/" + App.shared.getString("userid", "") + ".jpg", circleImageView , options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String url, View arg1) {

                }

                @Override
                public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                }

                @Override
                public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {

                }

                @Override
                public void onLoadingCancelled(String arg0, View arg1) {
                }
            });
        }

//        findViewById(R.id.imageView4).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                callUser();
//            }
//        });

        findViewById(R.id.floatingActionButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getPermission())
                    updateLocation();
            }
        });

        findViewById(R.id.imageView2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                call(App.shared.getString("userid" , ""));
            }
        });
//??? First you should find where the button is in the design
        contactAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ambulance_number.equals("null"))
                {
                    ambulance_number = "getting number";
                    new getNearestService().execute(String.valueOf(mLastLocation.getLatitude()) , String.valueOf(mLastLocation.getLongitude()));
                }
                else if(ambulance_number.equals("getting number"))
                {

                }
                else
                {
                    call(ambulance_number);
                }
            }
        });

        findViewById(R.id.end).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(Track.this , Home.class));
            }
        });

        new getSosDetails().execute(App.shared.getString("sosid" , ""));
    }

    private void call(String number) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+ number));
        if (ActivityCompat.checkSelfPermission(Track.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(Track.this,new String[]{Manifest.permission.CALL_PHONE},12);
            return;
        }
        startActivity(callIntent);
    }

    private void endEmergency() {
        //End Service
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                Track.this);

        // set title
        alertDialogBuilder.setTitle("End Service");

        // set dialog message
        alertDialogBuilder
                .setMessage("User ends the emergency Service. Do you want to close the service?")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        startActivity(new Intent(Track.this, Home.class));
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(new Intent(Track.this, Home.class));
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    void falseAlarm() {
        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(
                Track.this);

        // set title
        alertDialogBuilder2.setTitle("End Service");

        // set dialog message
        alertDialogBuilder2
                .setMessage("It is an false alarm. Do you want to close?")
                .setCancelable(false)
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                        startActivity(new Intent(Track.this, Home.class));
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                        startActivity(new Intent(Track.this, Home.class));
                    }
                });

        // create alert dialog
        AlertDialog alertDialog2 = alertDialogBuilder2.create();

        // show it
        alertDialog2.show();
    }


    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        userLocation = new LatLng(getIntent().getDoubleExtra("latitude", 0.0), getIntent().getDoubleExtra("longitude", 0.0));
        mMap.addMarker(new MarkerOptions().position(userLocation));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,16));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16), 2000, null);
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
    }

    public void drawRoute(final LatLng start, final LatLng end)
    {
        Log.e("route","yes");
        GoogleDirection.withServerKey("AIzaSyD7hazqNmiWUzXL3GwEa13ZvZLJq5-69Pc")
                .from(start)
                .to(end)
                .transportMode(TransportMode.DRIVING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        Log.e("method","inside");
                        if (direction.isOK())
                        {
                            // Do something
                            if (direction.isOK())
                            {
                                if (markerOptions1 == null)
                                {
                                    Log.e("marker1","yes");
                                    markerOptions1 = new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.police_car_topview));
                                    Log.e("marker2","yes");
                                    markerOptions2 = new MarkerOptions().position(end);

                                    marker1 = mMap.addMarker(markerOptions1);
                                    marker2 = mMap.addMarker(markerOptions2);
                                } else {
                                    marker1.setPosition(start);
                                    marker2.setPosition(end);
                                }

                                mMap.moveCamera(CameraUpdateFactory.newLatLng(start));

                                directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
                                if (polyline == null) {
                                    polyline = DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 5, Color.RED);
                                    myPolyline = mMap.addPolyline(polyline);
                                } else {
                                    myPolyline.setPoints(directionPositionList);
                                }

                                if(!reachedUser)
                                {
                                    getDistance(String.valueOf(start.latitude) , String.valueOf(start.longitude) ,
                                            String.valueOf(end.latitude) , String.valueOf(end.longitude));
                                }
                            }
                        }
                        else {
                            Log.e("test", direction.getStatus() + "1");
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                        Log.e("test", t.getMessage() + "22");
                    }
                });
    }

    public void updateLocation()
    {
        if (mLastLocation != null)
        {
            LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            new updatelocation().execute(App.shared.getString("id", ""), String.valueOf(latLng.latitude), String.valueOf(latLng.longitude));
            drawRoute(latLng , userLocation);
        }
        else
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (getPermission())
                updateLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendMessageToService(0);
        if (getIntent().getStringExtra("subject").equals("SOSES")) {
            endEmergency();
        } else if (getIntent().getStringExtra("subject").equals("SOSFA")) {
            falseAlarm();
        } else {
            automaticBind();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendMessageToService(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mGoogleApiClient==null)
        {
            buildGoogleApiClient();
            userLatitude = getIntent().getDoubleExtra("latitude", 0.0);
            userLongitude = getIntent().getDoubleExtra("longitude", 0.0);

            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000); // Update location every second

            userLocation = new LatLng(getIntent().getDoubleExtra("latitude", 0.0), getIntent().getDoubleExtra("longitude", 0.0));
        }
        mGoogleApiClient.connect();
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(getPermission())
            updateLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(getPermission())
            updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public boolean getPermission() {
        if (ContextCompat.checkSelfPermission(Track.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(Track.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Track.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 11);

            return false;
        } else {
            return true;
        }
    }

//getdetails fort php
//    public class getDetails extends AsyncTask<String, Void, String>
//    {
//        String results;
//
//        @Override
//        protected void onPreExecute()
//        {
//
//        }
//
//        @Override
//        protected String doInBackground(String... params)
//        {
//
//            try {
//                HttpClient http_client = new DefaultHttpClient();
//                HttpPost http_post = new HttpPost("http://139.59.24.15:3001/user/getUserDetails");
////                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/getContactDetails.php");
//                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
//                nameVP.add(new BasicNameValuePair("number" , params[0]));
//                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
//                HttpEntity entity = http_client.execute(http_post).getEntity();
//                if (entity != null) {
//                    String response = EntityUtils.toString(entity);
//                    Log.e("test", response+"michael");
//                    entity.consumeContent();
//                    http_client.getConnectionManager().shutdown();
//                    try
//                    {
//                        JSONObject object = new JSONObject(response);
//                        JSONArray jarray=object.getJSONArray("data");
//                        if(jarray.length()>0)
//                        {
//                            final JSONObject data = jarray.getJSONObject(0);
//                            name = data.getString("name");
//                            gender = data.getString("gender");
//                            dob = data.getString("dob");
//                            results = params[0];
//                        }
//                        else
//                        {
//                            results = "NoEvents";
//                        }
//                    }
//                    catch(Exception e)
//                    {
//                        Log.e("test" , e.toString());
//                        results = "NoEvents";
//                    }
//
//                }
//                else
//                {
//                    results = "Failure";
//                }
//            }
//            catch (Exception e)
//            {
//                results = e.toString();
//                Log.e("server", results);
//            }
//            return results;
//        }
//
//        @Override
//        protected void onPostExecute(final String result)
//        {
//            //update details.
//            nameage.setText(name+" , "+getAge(Integer.parseInt(dob.substring(0,4)) , Integer.parseInt(dob.substring(5,7)) , Integer.parseInt(dob.substring(8,10))));
//            gen.setText(gender);
//        }
//    }

    public class getDetails extends AsyncTask<String, Void, String>
    {
        String results;

        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected String doInBackground(String... params)
        {

            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port+"user/getUserDetails");
//                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/getContactDetails.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("number" , params[0]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test" , response);
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();
                    try
                    {
                        if(!response.equals("0")) {
                            final JSONObject data = new JSONObject(response);
                            name = data.getString("name");
                            gender = data.getString("gender");
                            dob = data.getString("dob");
                            medical = data.getString("medical");
                            results = params[0];
                        }
                        else
                        {
                            results = "Error";
                        }
                    }
                    catch(Exception e)
                    {
                        Log.e("test" , e.toString());
                        results = "NoEvents";
                    }

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
            if(!result.equals("Error"))
            {
                nameage.setText(name+" , "+getAge(Integer.parseInt(dob.substring(0,4)) , Integer.parseInt(dob.substring(5,7)) , Integer.parseInt(dob.substring(8,10))));
                gen.setText(gender);
            }
            else
            {
                Toast.makeText(getApplicationContext() , "Could not find user's data" , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class updatelocation extends AsyncTask<String, Void, String>
    {
        String results;

        @Override
        protected String doInBackground(String... params)
        {

            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port+"services/updateFireLocation");
//                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/updateLocation.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("id" , params[0]));
                nameVP.add(new BasicNameValuePair("latitude" , params[1]));
                nameVP.add(new BasicNameValuePair("longitude" , params[2]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test", response+"michael");
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();

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

                case App.END_EMERGENCY:
                    endEmergency();
                    break;

                case App.FALSE_ALARM:
                    //False Alarm
                    falseAlarm();
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
                    android.os.Message msg = android.os.Message.obtain(null, App.TRACK, intvaluetosend, 0);
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

    public int getAge (int _year, int _month, int _day)
    {
        GregorianCalendar cal = new GregorianCalendar();
        int y, m, d, a;

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(_year, _month, _day);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }
        if(a < 0)
            throw new IllegalArgumentException("Age < 0");
        return a;
    }

    @Override
    public void onBackPressed() {

    }

    //Send request to respective services
    public void sendRequestsToService() {
        for (int track = 0; track < services.size(); track++)
        {
            if(!App.shared.getString("id" , "").equals(services.get(track).getId()))
                senRequest(services.get(track).getId(), App.shared.getString("sosid", ""));
        }

        new getSosDetails().execute(App.shared.getString("sosid" , ""));
    }

    //and I am sending request to each ambulance as a smack(xmpp) message. UNderstud?
    //Yeah. Instead of geeting the ambulance details from server ans sending them a request from mobile app , sunayan
    //wants me to send the req directly from thew server
    //in that case we can reduce some time.
    private void senRequest(String to, String id) {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    // Send data as a String
                    Bundle bundle = new Bundle();
                    bundle.putString("to", to);
                    bundle.putString("id", id);
                    android.os.Message msg = android.os.Message.obtain(null, App.CALL_OTHER_SERVICE);
                    msg.setData(bundle);
                    mServiceMessenger.send(msg);
                } catch (Exception e) {
                    System.out.println(e.toString());
                }

            }
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

                final Handler handler = new Handler();
                final Runnable r = new Runnable()
                {
                    public void run()
                    {
                        new checkstatus().execute(App.shared.getString("sosid" , ""));
                    }
                };

                handler.postDelayed(r, 1000);
            }
            else if(result.equals("1"))
            {
                new getDetails().execute();
            }
            else
            {

            }
        }
    }

    public class getNearestService extends AsyncTask<String, Void, String> {
        String results;

        @Override
        protected void onPreExecute() {
            Toast.makeText(getApplicationContext() , "Fetching nearest ambulances near user" , Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port + "services/getnearestambulance");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                //i am passing lat and lon to server
                nameVP.add(new BasicNameValuePair("latitude", params[0]));
                nameVP.add(new BasicNameValuePair("longitude", params[1]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test", response);
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();
                    try {
                        //we wont get this data
                        JSONArray jarray = new JSONArray(response);
                        if (jarray.length() > 0) {
                            for (int i = 0; i < jarray.length(); i++) {
                                final JSONObject data = jarray.getJSONObject(i);
                                Service ambulance = new Service();
                                //and I am getting ambulance details from server
                                ambulance.setId(data.getString("ambulance_id"));
                                ambulance.setName(data.getString("contact_name"));
                                ambulance.setNumber(data.getString("contact_number"));
//                                    ambulance.setDistance(data.getDouble("distance"));
                                _id.add(data.getString("ambulance_id"));
                                services.add(ambulance);
                            }
                            results = "Success";
                        } else {
                            results = "NoEvents";
                        }
                    } catch (Exception e) {
                        Log.e("test", e.toString());
                        results = "NoEvents";
                    }
                } else {
                    results = "Failure";
                }
            } catch (Exception e) {
                results = e.toString();
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result) {
            Log.e("test", result);
            if (result.equals("Success")) {
                //and we wont send the req
                //I will send it in server using js
                sendRequestsToService();
            }
            else
            {
                Toast.makeText(getApplicationContext() , "Couldn't find any ambulance" , Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class getSosDetails extends AsyncTask<String, Void, String>
    {
        String results;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {

            try {
                HttpClient http_client = new DefaultHttpClient();
                HttpPost http_post = new HttpPost(Cons.port+"sos/getsosdetails");
//                HttpPost http_post = new HttpPost("http://139.59.24.15/ien/getsosdetails.php");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("sosid", params[0]));
                http_post.setEntity(new UrlEncodedFormEntity(nameVP));
                HttpEntity entity = http_client.execute(http_post).getEntity();
                if (entity != null) {
                    String response = EntityUtils.toString(entity);
                    Log.e("test" , response);
                    entity.consumeContent();
                    http_client.getConnectionManager().shutdown();

                    if(response.equals("0"))
                    {
                        results = "Not Found";
                    }
                    else
                    {
                        JSONObject data = new JSONObject(response);

                        if(!data.getString("ambulance_id").equals("null"))
                        {
                            ambulance_number = data.getString("ambulance_number");
                        }
                    }

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
                Log.e("server", results);
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {
            Log.e("tst" , result);
            Log.e("tst" , ambulance_number);
            if(ambulance_number.equals("getting number"))
            {

                final Handler handler = new Handler();
                final Runnable r = new Runnable()
                {
                    public void run()
                    {
                        new getSosDetails().execute(App.shared.getString("sosid" , ""));
                    }
                };

                handler.postDelayed(r, 1000);
            }
            else if(ambulance_number.equals("null"))
            {
                contactAmbulance.setImageDrawable(getResources().getDrawable(R.drawable.ambulance_alert));
            }
            else
            {
                contactAmbulance.setImageDrawable(getResources().getDrawable(R.drawable.ambulance_call));
            }
        }
    }

    public void getDistance(String lat1 , String lon1 , String lat2 , String lon2)
    {
        try
        {
            String Str_saferoute = "";
            String orgin = lat1 + "," + lon1;
            String destination = lat2 + "," + lon2;
            StringBuilder googlePlacesUrl = new StringBuilder("https://maps.googleapis.com/maps/api/distancematrix/json?");
            googlePlacesUrl.append("origins=" + orgin);
            googlePlacesUrl.append("&destinations=" + destination);
            googlePlacesUrl.append("&mode=" + "driving");
            googlePlacesUrl.append("&key=" + GOOGLE_API_KEY);
            GooglePlacesReadTask googlePlacesReadTask = new GooglePlacesReadTask();
            Object[] toPass = new Object[2];
            toPass[0] = mMap;
            toPass[1] = googlePlacesUrl.toString();
            googlePlacesReadTask.execute(toPass);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //This class used to get the  distance and duration between soure to destination usig goole api....................
    public class GooglePlacesReadTask extends AsyncTask<Object, Integer, String>
    {
        String googlePlacesData = null;
        GoogleMap googleMap;

        @Override
        protected String doInBackground(Object... inputObj) {
            try {
                googleMap = (GoogleMap) inputObj[0];
                String googlePlacesUrl = (String) inputObj[1];
                Http http = new Http();
                googlePlacesData = http.read(googlePlacesUrl);
            } catch (Exception e) {

            }
            return googlePlacesData;
        }

        @Override
        protected void onPostExecute(String result) {
            try
            {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray innerajsonarray = jsonObject.getJSONArray("rows");
                for (int i = 0; i < innerajsonarray.length(); i++) {
                    JSONObject innerjsonobj = innerajsonarray.getJSONObject(i);
                    JSONArray innerarray1 = innerjsonobj.getJSONArray("elements");
                    for (int j = 0; j < innerarray1.length(); j++) {
                        JSONObject innerjsonobj1 = innerarray1.getJSONObject(j);
                        JSONObject innerdistance = innerjsonobj1.getJSONObject("distance");
                        String strdistance = innerdistance.getString("text");
                        Integer intvalue = innerdistance.getInt("value");
                        JSONObject innerduration = innerjsonobj1.getJSONObject("duration");
                        String strduration_ = innerduration.getString("text");
                        Integer intdurationvalue = innerduration.getInt("value");
                        int minutes = intdurationvalue / 60;

                        if(intvalue<100)
                        {
                            reachedUser = true;
                            findViewById(R.id.end).setVisibility(View.VISIBLE);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}