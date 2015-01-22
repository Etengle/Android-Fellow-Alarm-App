package netdb.courses.softwarestudio.labs.pushnotification;

import android.app.AlertDialog;
import android.app.Dialog;
// import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
// import android.text.format.DateFormat;
import android.text.format.Time;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;


// import netdb.course.softwarestudio.service.rest.RestManager;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;
// import android.widget.ListView;

public class MainActivity extends ActionBarActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private final static String TAG = "MainActivity";
    private final static String SENDER_ID = "49003202870";

    private MediaPlayer mediaPlayer = null;
    private Vibrator vibrator;


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
    public static final String PROPERTY_REG_ID = "registration_id";
    public static String PhoneNumber = "117";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static final String FILENAME = "dom";


    private String snoozeTime [] = new String[] { "10 sec","30 sec","1 min", "3 min", "5 min", "10 min"};
    private String snoozeTimeinMills [] = new String[] { "10000", "30000", "60000", "180000", "300000", "600000"};

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
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.alarmsound);
        calendar.setTimeInMillis(System.currentTimeMillis());

        Time t = new Time();
        t.setToNow();
        hourOfDay = t.hour;
        minute = t.minute;

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(context);
            // TODO: Register your device
            if (regId.isEmpty()) {
                registerInBackground();
            }
            else{
                Log.i(TAG, "getStoredId:" + regId);
            }
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


                    // You may also want to store your id for later usage
                    storeRegistrationId(context, regId);

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


    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getSharedPreferences(
                MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID,
                "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        return registrationId;
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.apply();
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
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
            case R.id.action_settings:
                Dialog dialog;
                dialog = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_launcher3)
                        .setTitle("Choose snooze interval")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setItems(snoozeTime, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prefs = getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
                                editor = prefs.edit();
                                editor.putInt("snoozeInteval", Integer.parseInt(snoozeTimeinMills[which]));
                                editor.apply();
                            }
                        }).create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return true;
            case R.id.action_get:
                Intent intent2 = new Intent(this, ContactsActivity.class);
                // startActivity(intent);
                startActivityForResult(intent2, ContactsActivity.REQUEST_CODE);
                return true;
            case R.id.action_check:
                Toast.makeText(this, "Chosen phone number: " + PhoneNumber, Toast.LENGTH_SHORT).show();
                return true;
            case R.id.action_add:
                LayoutInflater factory = LayoutInflater.from(this);
                View myView = factory.inflate(R.layout.phone_dialog, null);

                final Dialog dialog2;
                /*dialog2.setContentView(R.layout.phone_dialog);*/
                final EditText edt = (EditText) myView.findViewById(R.id.editText3);
                dialog2 = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_launcher3)
                        .setTitle("Add phone number")
                        .setView(myView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String text = edt.getText().toString();
                                if(TextUtils.isEmpty(text)) {
                                    edt.setError("This field cannot be empty");
                                    Toast.makeText(MainActivity.this, "You should add a phone number before click OK.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    PhoneNumber = text;
                                    Toast.makeText(MainActivity.this, "Added phone number: " + PhoneNumber, Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();

                // dialog2.setCanceledOnTouchOutside(false);
                dialog2.show();
                return true;
            case R.id.action_help:
                Dialog dialog3;
                dialog3 = new AlertDialog.Builder(this)
                        .setIcon(R.drawable.ic_launcher3)
                        .setTitle("Help")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setMessage("This is a special alarm app." +
                                "\nWhen you snooze over 3 times constantly, " +
                                "it will CALL somebody else automatically " +
                                "\npreventing you from OVERSLEEPING.").create();
                // dialog3.setCanceledOnTouchOutside(false);
                dialog3.show();
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
            prefs = getSharedPreferences(FILENAME ,Context.MODE_PRIVATE);
            editor = prefs.edit();
            // int counter = prefs.getInt("counter", 0);
            editor.putInt("counter", 0);
            editor.apply();
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setAction("slighten.setalarm");
            Time t = new Time();
            t.setToNow();
            if (calendar.getTimeInMillis() - System.currentTimeMillis() < 0 &&
                    !(hourOfDay == t.hour && minute == t.minute)) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay+24);
                hourOfDay+=24;
            }
            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

            msg.setText("alarm at -- " + ((hourOfDay>=24)? hourOfDay-24 : hourOfDay)  + " : " + ((minute < 10) ? "0" + minute : minute) );
            // Toast.makeText(MainActivity.this, "sys: " + System.currentTimeMillis() +  "\ncal: " +  calendar.getTimeInMillis(), Toast.LENGTH_LONG).show();
            int diffhour = (minute-t.minute)<0 ? (hourOfDay-t.hour-1) : (hourOfDay-t.hour);
            int diffmin = (minute-t.minute)<0 ? (minute+60-t.minute) : (minute+-t.minute);
            if (diffhour != 0 || diffmin != 0)
                Toast.makeText(MainActivity.this, "Alarm after " +
                    (diffhour <= 1 ? (diffhour == 0 ? "" : diffhour + " hour ") : diffhour + " hours ") +
                    (diffmin <= 1 ? (diffmin == 0 ? "" :diffmin + " minute")  : diffmin + " minutes") , Toast.LENGTH_LONG).show();
            else
                Toast.makeText(MainActivity.this, "Alarm right away", Toast.LENGTH_LONG).show();
        }

    }
    private class DeleteOnClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            prefs = getSharedPreferences(FILENAME ,Context.MODE_PRIVATE);
            editor = prefs.edit();
            // Integer counter = prefs.getInt("counter", 0);
            editor.putInt("counter", 0);
            editor.apply();
                mediaPlayer.stop();
                // mediaPlayer.release();
                vibrator.cancel();

            if (AlarmMessage.alarm != null){
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("slighten.setalarm");
                PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmMessage.alarm.cancel(sender);
                MainActivity.this.msg.setText("Currently no alarm");
            }
            if (MainActivity.this.alarm != null) {
                Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
                intent.setAction("slighten.setalarm");
                PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                MainActivity.this.alarm.cancel(sender);
                MainActivity.this.msg.setText("Currently no alarm");
                Toast.makeText(MainActivity.this, "delete successfully", Toast.LENGTH_SHORT).show();
            }
            delete.setText("delete alarm");

        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            PhoneNumber = data.getStringExtra("PhoneNumber");
            // Toast.makeText(this, PhoneNumber, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Please choose a contact person", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        prefs = getSharedPreferences(FILENAME ,Context.MODE_PRIVATE);
        int counter = prefs.getInt("counter", 0);
        if (counter >= 3)
            delete.setText("DELETE ALARM");
        if (prefs.getInt("call", 0) == 1) {
            prefs = getSharedPreferences(FILENAME ,Context.MODE_PRIVATE);
            editor = prefs.edit();
            editor.putInt("call", 0);
            editor.apply();
            int snoozeIntevalinMills = prefs.getInt("snoozeInteval", 0);
            snoozeIntevalinMills = (snoozeIntevalinMills < 5000) ? 60000 : snoozeIntevalinMills;
            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setAction("slighten.setalarm");
            PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + snoozeIntevalinMills, sender);
        }
    }
    protected void onDestroy() {
        super.onDestroy();
        prefs = getSharedPreferences(FILENAME ,Context.MODE_PRIVATE);
        editor = prefs.edit();
        editor.putInt("call", 0);
        editor.putInt("counter", 0);
        editor.apply();
    }
}
