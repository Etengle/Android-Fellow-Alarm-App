package netdb.courses.softwarestudio.labs.pushnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Slighten on 2015/1/19.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        StaticWakeLock.lockOn(context);
        Intent it = new Intent(context, AlarmMessage.class);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(it);
    }

}

