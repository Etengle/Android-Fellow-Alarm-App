package netdb.courses.softwarestudio.labs.pushnotification;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;


import netdb.course.softwarestudio.service.rest.RestManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = "MainActivity";
    private final static String SENDER_ID = "49003202870";


    private ArrayList<User> mUserList = new ArrayList<User>();
    private ListView mListView;
    private UserAdapter mUserAdapter;

    private GoogleCloudMessaging gcm;
    static protected String regId = null;
    private Context context;

    private AlarmManager alarm = null;
    private Button set = null;
    private Button delete = null;
    private TextView msg = null;
    private TimePicker time = null;
    private int hourOfDay = 0;
    private int minute = 0;
    private Calendar calendar = Calendar.getInstance();
    private RestManager restMgr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        set = (Button) findViewById(R.id.set);
        delete = (Button) findViewById(R.id.delete);
        msg = (TextView) findViewById(R.id.msg);
        time = (TimePicker) findViewById(R.id.time);

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        set.setOnClickListener(new SetOnClickListener());
        delete.setOnClickListener(new DeleteOnClickListener());
        time.setIs24HourView(true) ;
        time.setOnTimeChangedListener(new OnTimeChangedListenerImpl());

        restMgr = RestManager.getInstance(this);



        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);

            // TODO: Register your device
            registerInBackground();
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();

            } else {
                Log.i(TAG, "This device is not supported.");
                // Disable some functions of your app
            }

            return false;
        }
        return true;
    }

    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
                    // TODO: obtain your device id and assign it to regId
                    regId = gcm.register(SENDER_ID);

                    msg = "Device registered, registration ID=" + regId;



                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                Log.i(TAG, msg);
            }
        }.execute(null, null, null);
    }


    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the action bar items pressed
        switch (item.getItemId()) {
            case R.id.action_add:
                Intent intent = new Intent(this, PostUserActivity.class);
                // startActivity(intent);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class OnTimeChangedListenerImpl implements OnTimeChangedListener{

        @Override
        public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            MainActivity.this.hourOfDay = hourOfDay;
            MainActivity.this.minute = minute;
        }

    }

    private class SetOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setAction("slighten.setalarm");
            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
            msg.setText("Alarm time: " + hourOfDay + ":" + minute);
            Toast.makeText(MainActivity.this, "set successfully", Toast.LENGTH_LONG).show();
        }

    }
    private class DeleteOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            if (MainActivity.this.alarm != null) {
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("slighten.setalarm");
                PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                MainActivity.this.alarm.cancel(sender);
                MainActivity.this.msg.setText("no alarm");
                Toast.makeText(MainActivity.this, "delete successfully", Toast.LENGTH_LONG).show();
            }
        }

    }

}
