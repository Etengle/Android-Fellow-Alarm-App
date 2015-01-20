package netdb.courses.softwarestudio.labs.pushnotification;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.os.Vibrator;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
/**
 * Created by Slighten on 2015/1/19.
 */
public class AlarmMessage extends ActionBarActivity {

    private Context context;
    private MediaPlayer mediaPlayer = null;
    private Vibrator vibrator;
    private AudioManager audio = null;
    protected static AlarmManager alarm = null;
    private TextView msg = null;
    private Calendar calendar = Calendar.getInstance();
    static public SharedPreferences prefs;
    static public SharedPreferences.Editor editor;
    private int counter;
    private static final String FILENAME = "dom";
    private static final int ONESEC = 1000;
    private static final int ONEMIN = 60000;
    private int snoozeIntevalinMills = 60000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        msg = (TextView) findViewById(R.id.msg);
        // setContentView(R.layout.activity_main);
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
        audio.adjustVolume(AudioManager.ADJUST_RAISE, 0);
        mediaPlayer = MediaPlayer.create(AlarmMessage.this, R.raw.alarmsound);
        mediaPlayer.setVolume(1.0f, 1.0f);
        mediaPlayer.setLooping(true);
        /*try {
            mediaPlayer.prepare();
        } catch (Exception e){
            mediaPlayer.release();
        }*/
        mediaPlayer.start();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] { 0, 2000, 500 }, 0);
        Dialog dialog = new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("時間到囉!")
                .setMessage(new SimpleDateFormat("現在是 H 點 mm 分 ss 秒")
                        .format(new Date(System.currentTimeMillis())))
                .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancelAlarm();
                    }
                }).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setOnKeyListener(new Dialog.OnKeyListener() {

            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    cancelAlarm();
                }
                return true;
            }
        });
    }

    private void cancelAlarm(){
        prefs = getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
        counter = prefs.getInt("counter", 0);
        snoozeIntevalinMills = prefs.getInt("snoozeInteval", 0);
        snoozeIntevalinMills = (snoozeIntevalinMills < 5000) ? 60000 : snoozeIntevalinMills;

        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(AlarmMessage.this, AlarmReceiver.class);
        intent.setAction("slighten.setalarm");
        PendingIntent sender = PendingIntent.getBroadcast(AlarmMessage.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + snoozeIntevalinMills, sender);
        // msg.setText("Alarm time: " + calendar.get + ":" + calendar.MINUTE+1);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            // mediaPlayer.release();
        }
            vibrator.cancel();
        int min = snoozeIntevalinMills/ONEMIN;
        int sec = (snoozeIntevalinMills%ONEMIN) / ONESEC;
        if (counter < 2) {
            Toast.makeText(AlarmMessage.this, "Snooze after " + ((min > 0) ? min + " minute" : sec + " second"), Toast.LENGTH_SHORT).show();
            editor.putInt("counter", ++counter);
            editor.apply();
        }else if (counter == 2) {
            Toast.makeText(AlarmMessage.this, "Snooze again" + ((min > 0) ? min + " minute" : sec + " second") + ",\nand then it'll call someone else.", Toast.LENGTH_SHORT).show();
            editor.putInt("counter", ++counter);
            editor.apply();
        }else {
            Toast.makeText(AlarmMessage.this, "Snooze again" + ((min > 0) ? min + " minute" : sec + " second") + ",\nand then it'll call someone else.", Toast.LENGTH_SHORT).show();
            String telStr = MainActivity.PhoneNumber;
            Uri uri = Uri.parse("tel:" + telStr);
            Intent it = new Intent();
            it.setAction(Intent.ACTION_CALL);
            it.setData(uri);
            startActivity(it);
        }

        AlarmMessage.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cancelAlarm();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // cancelAlarm();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // cancelAlarm();
    }
}
