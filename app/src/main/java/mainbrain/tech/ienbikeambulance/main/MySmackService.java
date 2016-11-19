package mainbrain.tech.ienbikeambulance.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xevent.MessageEventManager;
import org.jivesoftware.smackx.xevent.MessageEventNotificationListener;
import org.jivesoftware.smackx.xevent.MessageEventRequestListener;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import mainbrain.tech.ienbikeambulance.R;
import mainbrain.tech.ienbikeambulance.track.ServiceRequest;
import mainbrain.tech.ienbikeambulance.track.Track;

public class MySmackService extends Service implements StanzaListener, MessageListener
{
    String SMACK="SMACK";
    private List<Messenger> mClients = new ArrayList<Messenger>();
    private XMPPTCPConnectionConfiguration config;
    public static XMPPTCPConnection connection;
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SET_INT_VALUE = 3;
    public static final int MSG_SET_STRING_VALUE = 4;
    private static boolean isRunning = false;
    Messenger mMessenger = new Messenger(new IncomingMessageHandler());
    MessageEventManager m;
    private boolean selectserviceOnline;
    private String android_id;
    private MultiUserChat muc;
    private boolean trackOnline;
    private boolean homeOnline;

    @Override
    public void onCreate()
    {
        isRunning = true;
        super.onCreate();

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);

        config= XMPPTCPConnectionConfiguration
                .builder()
                .setHost("139.59.27.47")
                .setPort(5222)
                .setServiceName("localhost")
                .setSecurityMode(SecurityMode.disabled)
                .setDebuggerEnabled(false)
                .build();

        connection=new XMPPTCPConnection(config);

        connection.addAsyncStanzaListener(this, MessageTypeFilter.NORMAL_OR_CHAT_OR_HEADLINE);

        //connection.setUseStreamManagement(true);

        //connection.setUseStreamManagementResumption(true);

        Log.e("test" , App.shared.getString("id", "test1")+"smackid");

        new connections().execute(App.shared.getString("id", "test1"), App.shared.getString("id", "test1"));

        connection.addConnectionListener(new ConnectionListener()
        {
            @Override
            public void reconnectionSuccessful() {

            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                Log.e(SMACK, "Reconnection Failed");
            }

            @Override
            public void reconnectingIn(int arg0) {
                Log.e(SMACK, "Reconnecting in " + arg0);
            }

            @Override
            public void connectionClosedOnError(Exception arg0) {
                Log.e(SMACK, "Connection cloesd" + arg0.toString());
            }

            @Override
            public void connectionClosed() {
                Log.e(SMACK, "Connection closed");
            }

            @Override
            public void connected(XMPPConnection arg0) {
                Log.e(SMACK, "Connected");
            }

            @Override
            public void authenticated(XMPPConnection arg0, boolean arg1) {
                Log.e(SMACK, "authendicated");
//
//                OfflineMessageManager off = new OfflineMessageManager(connection);
//                try
//                {
//                    Log.e(SMACK, String.valueOf(off.getMessageCount())+"michael ajsions iadjnosbfiubfiewu");
//                }
//                catch (Exception e)
//                {
//
//                }
            }
        });

        connection.addStanzaAcknowledgedListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza arg0) throws NotConnectedException {

            }
        });

        ReconnectionManager manager= ReconnectionManager.getInstanceFor(connection);
        manager.enableAutomaticReconnection();

        m= MessageEventManager.getInstanceFor(connection);

        m.addMessageEventNotificationListener(new MessageEventNotificationListener() {
            @Override
            public void offlineNotification(String arg0, String arg1) {
                Log.e(SMACK, arg0 + arg1 + "offline");
            }

            @Override
            public void displayedNotification(String arg0, String arg1) {
                Log.e(SMACK, arg0 + arg1 + "displayed");
            }

            @Override
            public void deliveredNotification(String arg0, String arg1) {
                Log.e(SMACK, arg0 + arg1 + "deliverd");
            }

            @Override
            public void composingNotification(String arg0, String arg1) {
                Log.e(SMACK, arg0 + arg1 + "composing");

            }

            @Override
            public void cancelledNotification(String arg0, String arg1) {
                Log.e(SMACK, arg0 + arg1 + "cabcel");

            }
        });

        m.addMessageEventRequestListener(new MessageEventRequestListener() {

            @Override
            public void offlineNotificationRequested(String arg0, String arg1,
                                                     MessageEventManager arg2) {
                Log.e(SMACK, arg0 + arg1 + "o111ffline");
            }

            @Override
            public void displayedNotificationRequested(String arg0, String arg1,
                                                       MessageEventManager arg2) {
                Log.e(SMACK, arg0 + arg1 + "d111isplayed");
            }

            @Override
            public void deliveredNotificationRequested(String arg0, String arg1,
                                                       MessageEventManager arg2) throws NotConnectedException {
                Log.e(SMACK, arg0 + arg1 + "d111eliverd");
            }

            @Override
            public void composingNotificationRequested(String arg0, String arg1,
                                                       MessageEventManager arg2) {
                Log.e(SMACK, arg0 + arg1 + "c111omposing");
            }
        });

    }

    public void sendAccepted(String to , String content)
    {
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject("FSOSA");
        message.setBody(content);
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {

        }
    }

    public void sendAcceptedFromAnotherService(String to , String content)
    {
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject(content);
        message.setBody("ASOSAFS");
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {

        }
    }

    public void sendRejectedFromAnotherService(String to , String content)
    {
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject(content);
        message.setBody("ASOSRFS");
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {

        }
    }

    public void sendRejected(String to , String content)
    {
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject("ASOSR");
        message.setBody(content);
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    @Override
    public void processPacket(Stanza arg0) throws NotConnectedException
    {
        Message message = (Message) arg0;

        Log.e("test" , message.toString());

        if(message.getSubject().equals("SOSR"))
        {
            //here we are passing the message body
            new getSosDetails().execute(message.getBody());
            m.sendDeliveredNotification(message.getFrom() , message.getPacketID());
        }
        else if(message.getSubject().equals("SOSES") && App.shared.getString("sosid" , "").equals(message.getBody()))
        {
            if(trackOnline)
            {
                sendEndService(message.getBody());
            }
            else
            {
                endserviceNotification(message.getBody());
            }
            m.sendDeliveredNotification(message.getFrom() , message.getPacketID());
        }
        else if(message.getSubject().equals("SOSFA") && App.shared.getString("sosid" , "").equals(message.getBody()))
        {
            if(trackOnline)
            {
                sendFalseAlarm(message.getBody());
            }
            else
            {
                falseAlarmNotification(message.getBody());
            }
            m.sendDeliveredNotification(message.getFrom() , message.getPacketID());
        }
        else if(message.getSubject().equals("SOSRFS"))
        {
            new getSosDetails().execute(message.getBody());
            m.sendDeliveredNotification(message.getFrom() , message.getPacketID());
        }

//        if(message.getBody().contains("SOSES")&&message.getSubject().equals(App.shared.getString("sosid" , "")))
//        {
//            Log.e("test" , "tessting");
//            sendEndService(message.getSubject());
//        }
//        else if(message.getBody().contains("SOSFA")&&message.getSubject().equals(App.shared.getString("sosid" , "")))
//        {
//            sendFalseAlarm(message.getSubject());
//        }
//        else if(message.getBody().substring(0,6).contains("SOSRFS"))
//        {
//            String latitude = message.getBody().substring(6 , message.getBody().indexOf(","));
//            String longitude = message.getBody().substring(message.getBody().indexOf(",")+1 , message.getBody().length());
//            notification(message.getSubject() , message.getBody().substring(0,1),message.getSubject() , latitude , longitude);
//        }
//        else if(message.getBody().contains("ASOSAFS"))
//        {
//            AcceptedFromAnotherService(message.getFrom() , message.getBody());
//        }
//        else if(message.getBody().contains("ASOSRFS"))
//        {
//            RejectedFromAnotherService(message.getFrom() , message.getBody());
//        }
//        else if(message.getBody().contains("ASOSR"))
//        {
//            sendRejected(message.getBody());
//        }
//        else if(message.getBody().substring(1,5).equals("SOSR"))
//        {
//            String latitude = message.getBody().substring(5 , message.getBody().indexOf(","));
//            String longitude = message.getBody().substring(message.getBody().indexOf(",")+1 , message.getBody().length());
//            notification(message.getSubject() , message.getBody().substring(0,1) , message.getFrom() , latitude , longitude);
//        }

//        Database db = new Database(getApplicationContext());

//        db.open();
//        db.addmsg(message.getStanzaId(),message.getFrom(),message.getBody(),getdate(),gettime(),"0" , "from");
//        db.close();

//        if(!selectserviceOnline)
//        {
//            Intent intent1 = new Intent(getApplicationContext(),Home.class);
//            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
//                    .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("New task assigned")
//                    .setContentIntent(pendingNotificationIntent)
//                    .setAutoCancel(true)
//                    .setLights(Color.RED, 3000, 3000)
//                    .setContentText(message.getBody());
//
//            Notification notification=mBuilder.build();
//            notification.defaults |= Notification.DEFAULT_SOUND;
//            notification.defaults |= Notification.DEFAULT_VIBRATE;
//
//            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.notify(1, notification);
//        }
//        else
//        {
//            updateUI(message.getStanzaId());
//        }
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mMessenger.getBinder();
    }

    @Override
    public void processMessage(Message message)
    {

//        App.db.open();
//        App.db.addmsg(message.getStanzaId(),message.getFrom(),message.getBody(),getdate(),gettime(),"0" , "from");
//        App.db.close();

//        if(!selectserviceOnline)
//        {
//            Intent intent1 = new Intent(getApplicationContext(),Home.class);
//            intent1.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP| Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent1, PendingIntent.FLAG_UPDATE_CURRENT);
//
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext())
//                    .setSmallIcon(R.mipmap.ic_launcher).setContentTitle("New task assigned")
//                    .setContentIntent(pendingNotificationIntent)
//                    .setAutoCancel(true)
//                    .setLights(Color.RED, 3000, 3000)
//                    .setContentText(message.getBody());
//
//            Notification notification=mBuilder.build();
//            notification.defaults |= Notification.DEFAULT_SOUND;
//            notification.defaults |= Notification.DEFAULT_VIBRATE;
//
//            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//            mNotificationManager.notify(1, notification);
//        }
//        else
//        {
//            updateUI(message.getStanzaId());
//        }
    }

    private class IncomingMessageHandler extends Handler
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch (msg.what)
            {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;

                case MSG_UNREGISTER_CLIENT:
                    mClients.remove(msg.replyTo);
                    break;

                case App.HOME:
                    Log.e("test" , "home");
                    if(msg.arg1==0)
                        homeOnline=true;
                    else
                        homeOnline=false;
                    break;

                case App.FCM:
                    break;

                case App.SERVICEREQUEST:
                    if(msg.arg1==0)
                        selectserviceOnline=true;
                    else
                        selectserviceOnline=false;
                    break;

                case App.TRACK:
                    if(msg.arg1==0)
                        trackOnline=true;
                    else
                        trackOnline=false;
                    break;

                case App.WAY_TO_HOSPITAL:
                    if(msg.arg1==0)
                        trackOnline=true;
                    else
                        trackOnline=false;
                    break;

                case App.FORWARD_SOS:
                    sendtoanotherservice(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                case App.CALL_OTHER_SERVICE:
                    callotherservices(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                case App.SERVICEREQUEST_ACCEPT:
                    sendAccepted(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                case 111:
                    sendAcceptedFromAnotherService(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                case App.SERVICEREQUEST_REJECT:
                    sendRejected(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                case 121:
                    sendRejectedFromAnotherService(msg.getData().getString("to") , msg.getData().getString("id"));
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    public class connections extends AsyncTask<String, Void, String>
    {
        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                connection.connect();
                connection.login(params[0],params[1]);
            }
            catch(Exception e)
            {
                Log.e("test" , e.toString()+"COnnection");
            }
            return "";
        }
    }

    public String getdate()
    {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
        String time = sdt.format(currentLocalTime);
        return time;
    }

    public String gettime()
    {
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        SimpleDateFormat sdt = new SimpleDateFormat("HH:mm a");
        String time = sdt.format(currentLocalTime);
        return time;
    }

    private void AcceptedFromAnotherService(String id , String content)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("id",id);
                bundle.putString("content",content);
                android.os.Message msg = android.os.Message.obtain(null, 112);
                msg.setData(bundle);
                messenger.send(msg);
                Log.e("test" , "message send to activity");
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                Log.e("test" , e.toString());
            }
        }
    }

    private void RejectedFromAnotherService(String from , String content)
    {
        Log.e("test" , "rq");
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("id",from);
                bundle.putString("content",content);
                android.os.Message msg = android.os.Message.obtain(null, 113);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    private void sendEndService(String content)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("id",content);
                android.os.Message msg = android.os.Message.obtain(null, App.END_EMERGENCY);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    private void sendFalseAlarm(String content)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("id",content);
                android.os.Message msg = android.os.Message.obtain(null, App.FALSE_ALARM);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    private void sendRejected(String content)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("id",content);
                android.os.Message msg = android.os.Message.obtain(null, 32);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    private void sendUserLocation(Double latitude , Double longitude)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putDouble("latitude",latitude);
                bundle.putDouble("longitude",longitude);
                android.os.Message msg = android.os.Message.obtain(null, 31);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    private void sendsosrequest(String sosid , String self ,  String userid , Double latitude , Double longitude)
    {
        Iterator<Messenger> messengerIterator = mClients.iterator();
        while(messengerIterator.hasNext())
        {
            Messenger messenger = messengerIterator.next();
            try
            {
                Bundle bundle = new Bundle();
                bundle.putString("sosid",sosid);
                bundle.putString("self",self);
                bundle.putString("userid",userid);
                bundle.putDouble("latitude",latitude);
                bundle.putDouble("longitude",longitude);
                android.os.Message msg = android.os.Message.obtain(null, App.HOME_REQUEST);
                msg.setData(bundle);
                messenger.send(msg);
            }
            catch (RemoteException e)
            {
                mClients.remove(messenger);
                System.out.println(e.toString());
            }
        }
    }

    public void sendtoanotherservice(String to , String content)
    {
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject("SOSRFS");
        message.setBody(content);
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {
            Log.e("smackError" , e.toString());
        }
    }

    public void callotherservices(String to , String content)
    {
        Log.e("tezt" , to);
        Message message = new Message(to+"@localhost", Message.Type.chat);
        message.setSubject("SOSR");
        message.setBody(content);
        try
        {
            connection.sendStanza(message);
        }
        catch(Exception e)
        {
            Log.e("smackError" , e.toString());
        }
    }

    public void falseAlarmNotification(String sosid)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("End of SOS")
                        .setContentText("User accidently ask for ERS service");
        mBuilder.setAutoCancel(true);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        Intent resultIntent = new Intent(this, Track.class);
        resultIntent.putExtra("subject" , "SOSES");
        resultIntent.putExtra("sosid" , sosid);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(ServiceRequest.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(App.notificationID, mBuilder.build());
    }

    public void endserviceNotification(String sosid)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("End of SOS")
                        .setContentText("User ends the sos service");
        mBuilder.setAutoCancel(true);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
        Intent resultIntent = new Intent(this, Track.class);
        resultIntent.putExtra("subject" , "SOSES");
        resultIntent.putExtra("sosid" , sosid);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(App.notificationID, mBuilder.build());
    }

    public void notification(String subject , String self , String userid , String latitude , String longitude)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.common_ic_googleplayservices)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");
        mBuilder.setAutoCancel(true);
        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
// Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, ServiceRequest.class);
        resultIntent.putExtra("sosid" , subject);
        resultIntent.putExtra("userid" , userid);
        resultIntent.putExtra("self" , self);
        resultIntent.putExtra("latitude" , Double.parseDouble(latitude));
        resultIntent.putExtra("longitude" , Double.parseDouble(longitude));

// The stack builder object will contain an artificial back stack for the
// started Activity.
// This ensures that navigating backward from the Activity leads out of
// your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
// Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(ServiceRequest.class);
// Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
        mNotificationManager.notify(App.notificationID, mBuilder.build());
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
                //which is have to be an sos id to get the sos details like
                //so what can I get , If i send happy :) as sosid?
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
//                        JSONObject object = new JSONObject(response);
                        JSONObject data = new JSONObject(response);

                        //if home i snot onliune then we will show a notification
                        if(!homeOnline) {
                            //self , userid , latitude and longitude
                            Log.e("lat" , data.getString("latitude"));
                            notification(params[0], data.getString("self"), data.getString("userid"), data.getString("latitude"), data.getString("longitude"));
                        }
                        else {
                            Log.e("lat" , data.getString("latitude"));
                            sendsosrequest(params[0], data.getString("self"), data.getString("userid"), data.getDouble("latitude"), data.getDouble("longitude"));
                        }
                    }

                    results = response;
                } else {
                    results = "Failure";
                }
            } catch (Exception e) {
                results = e.toString();
                Log.e("server", results);
            }
            return results;
        }

        @Override
        protected void onPostExecute(final String result)
        {

            Log.e("t" , result);
        }
    }
}
