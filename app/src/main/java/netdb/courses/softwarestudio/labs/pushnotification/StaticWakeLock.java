package netdb.courses.softwarestudio.labs.pushnotification;

import android.content.Context;
import android.os.PowerManager;

/**
 * Created by Slighten on 2015/1/19.
 */
public class StaticWakeLock {
    private static PowerManager.WakeLock wl = null;

    public static void lockOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        //Object flags;
        if (wl == null)
            wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "alarm");
        wl.acquire();
    }

    public static void lockOff(Context context) {
//		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        try {
            if (wl != null)
                wl.release();
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }
}