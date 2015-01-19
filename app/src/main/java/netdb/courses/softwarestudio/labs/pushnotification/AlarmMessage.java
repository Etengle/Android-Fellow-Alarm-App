package netdb.courses.softwarestudio.labs.pushnotification;


import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.Context;
import android.os.Vibrator;
import java.util.Calendar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.widget.Toast;
/**
 * Created by Slighten on 2015/1/19.
 */
public class AlarmMessage extends Activity {

    private Context context;
    private MediaPlayer mediaPlayer = null;
    private Vibrator vibrator;
    private AudioManager audio = null;
    private AlarmManager alarm = null;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new AlertDialog.Builder(this).setIcon(R.drawable.ic_launcher).setTitle("時間到囉!")
                .setMessage(new SimpleDateFormat("現在是 H 點 mm 分 ss 秒")
                        .format(new Date(System.currentTimeMillis())))
                .setPositiveButton("Snooze", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                        Intent intent = new Intent(AlarmMessage.this, AlarmReceiver.class);
                        intent.setAction("slighten.setalarm");
                        PendingIntent sender = PendingIntent.getBroadcast(AlarmMessage.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + 60000, sender);
                        Toast.makeText(AlarmMessage.this, "Snooze after 1 minute", Toast.LENGTH_LONG).show();
                        mediaPlayer.stop();
                        mediaPlayer.release();
                        vibrator.cancel();
                        AlarmMessage.this.finish();
                    }
                }).show();
    }

}
