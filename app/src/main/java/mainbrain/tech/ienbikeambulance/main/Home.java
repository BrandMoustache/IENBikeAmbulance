package mainbrain.tech.ienbikeambulance.main;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import mainbrain.tech.ienbikeambulance.track.MapWrapperLayout;
import mainbrain.tech.ienbikeambulance.track.ServiceRequest;

public class Home extends AppCompatActivity implements OnMapReadyCallback, ServiceConnection, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    private Marker marker;

    Button onoff;
    boolean online = false;

    private Messenger mServiceMessenger = null;
    boolean mIsBound;
    private static final String LOGTAG = "MainActivity";
    private final Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    private ServiceConnection mConnection = this;

    ImageView satellite, map;
    String id;
    private String address = "Fetching address";
    private AddressResultReceiver mResultReceiver;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;

    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        getActionBar().hide();

        if (!MySmackService.isRunning()) {
            Log.e("Home","callingsmack");
            startService(new Intent(Home.this, MySmackService.class));
        }

        automaticBind();

        onoff = (Button) findViewById(R.id.button10);

        satellite = ImageView.class.cast(findViewById(R.id.satellite));
        map = ImageView.class.cast(findViewById(R.id.map));

        mResultReceiver = new AddressResultReceiver(new Handler());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.location_map);
        mapFragment.getMapAsync(this);

        onoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeStatus();
            }
        });

        new Sansation().overrideFonts(getApplicationContext(), onoff);

        satellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    satellite.setBackground(getResources().getDrawable(R.drawable.satellitecolor));
                } else {
                    satellite.setBackgroundDrawable(getResources().getDrawable(R.drawable.satellitecolor));
                }
                satellite.setColorFilter(getResources().getColor(R.color.textColorWhite));
                map.setColorFilter(getResources().getColor(R.color.textcolor));
                map.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        buildGoogleApiClient();

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    map.setBackground(getResources().getDrawable(R.drawable.mapcolor));
                } else {
                    map.setBackgroundDrawable(getResources().getDrawable(R.drawable.mapcolor));
                }
                map.setColorFilter(getResources().getColor(R.color.textColorWhite));
                satellite.setColorFilter(getResources().getColor(R.color.textcolor));
                satellite.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second
    }

    private void changeStatus() {
        if (online) {
            updateLocation();
            onoff.setText("GO ONLINE");
            onoff.setBackgroundColor(Color.parseColor("#b2b2b2"));
            new changeStatus().execute(App.shared.getString("id", ""), "0");
            new updatelocation().execute(App.shared.getString("id", ""), String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        } else {
            updateLocation();
            onoff.setText("GO OFFLINE");
            onoff.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            new changeStatus().execute(App.shared.getString("id", ""), "1");
            new updatelocation().execute(App.shared.getString("id", ""), String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        }
        online = !online;
    }

    public boolean getPermission() {
        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Home.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 11);

            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 11: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateLocation();
                } else {

                }
                return;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(Home.this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, 11);
            return;
        }

        infoWindowSetup(mMap);

        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    public void updateLocation()
    {
        if (mLastLocation != null)
        {
            LatLng myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (marker == null)
            {
                marker = mMap.addMarker(new MarkerOptions().position(myLocation)
                        .title("Your address")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.police_car_topview))
                        .flat(true)
                        .draggable(false)
                        .anchor(0.5f, 0.5f)
                        .rotation(mLastLocation.getBearing()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16.0f));
            }
            else
            {
                marker.setPosition(myLocation);
            }
            startIntentService(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        }
        else
        {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                getPermission();
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateLocation();
        }
    }

    @Override
    public void onStop() {
        sendMessageToService(1);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            mGoogleApiClient.disconnect();
            sendMessageToService(1);
            doUnbindService();
        } catch (Throwable t) {
            Log.e(LOGTAG, "Failed to unbind from the service", t);
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        sendMessageToService(0);

        if (mMap != null)
            updateLocation();

        super.onResume();
    }

//Code to connect with smackservice
//
    private void automaticBind() {
        doBindService();
    }

    private void doBindService() {
        bindService(new Intent(this, MySmackService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
        sendMessageToService(0);
    }

    private void doUnbindService() {
        if (mIsBound) {
            if (mServiceMessenger != null) {
                try {
                    android.os.Message msg = android.os.Message.obtain(null, MySmackService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mServiceMessenger.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service has crashed.
                }
            }
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mServiceMessenger = new Messenger(service);
        try {
            android.os.Message msg = android.os.Message.obtain(null, MySmackService.MSG_REGISTER_CLIENT);
            msg.replyTo = mMessenger;
            mServiceMessenger.send(msg);
        } catch (RemoteException e) {
            // In this case the service has crashed before we could even do anything with it
        }
        sendMessageToService(0);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mServiceMessenger = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(getPermission())
            updateLocation();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.e("e" , "conneccted");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        Log.e("test" , mLastLocation.getLongitude()+"");

        if(getPermission())
            updateLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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

                case App.HOME_REQUEST:

                    Intent resultIntent = new Intent(Home.this, ServiceRequest.class);
                    resultIntent.putExtra("sosid" , msg.getData().getString("sosid"));
                    resultIntent.putExtra("userid" , msg.getData().getString("userid"));
                    resultIntent.putExtra("self" , msg.getData().getString("self"));
                    resultIntent.putExtra("latitude" , msg.getData().getDouble("latitude"));
                    resultIntent.putExtra("longitude" , msg.getData().getDouble("longitude"));

                    startActivity(resultIntent);

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
                    android.os.Message msg = android.os.Message.obtain(null, App.HOME, intvaluetosend, 0);
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

    //update ERS location in the server
    public class updatelocation extends AsyncTask<String, Void, String>
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

        }
    }

    //Chnage ERS status in the server
    public class changeStatus extends AsyncTask<String, Void, String>
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
                HttpPost http_post = new HttpPost(Cons.port+"services/changeFireStatus");
                List<NameValuePair> nameVP = new ArrayList<NameValuePair>(2);
                nameVP.add(new BasicNameValuePair("id" , params[0]));
                nameVP.add(new BasicNameValuePair("status" , params[1]));
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

        }
    }

    //Code to get the address from latitude and longitude
    protected void startIntentService(LatLng mLastLocation)
    {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    class AddressResultReceiver extends ResultReceiver
    {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData)
        {
            address = resultData.getString(Constants.RESULT_DATA_KEY);
            marker.setSnippet(address);
        }
    }

    private void infoWindowSetup(GoogleMap googlemap)
    {
        final MapWrapperLayout mapWrapperLayout = (MapWrapperLayout) findViewById(R.id.map_relative_layout);

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        mapWrapperLayout.init(googlemap, getPixelsFromDp(getApplication(), 39 + 20));

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instancend
        //this is the layout  with custom title / text color and text
        this.infoWindow = (ViewGroup) getLayoutInflater().inflate(R.layout.custominfowindow, null);
        this.infoTitle = (TextView) infoWindow.findViewById(R.id.title);
        this.infoAddress = (TextView) infoWindow.findViewById(R.id.address);

        new Sansation().overrideFonts(this, infoTitle);
        new Sansation().overrideFonts(this, infoAddress);

        //Custom marker Window.............
        googlemap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoTitle.setText(marker.getTitle());
                infoAddress.setText(marker.getSnippet());

                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                infoWindow.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                return infoWindow;
            }
        });
    }

    public int getPixelsFromDp(Context context, float dp)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}