package kr.dude.newtag;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootupReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "BootupReceiver";

    public BootupReceiver() {
        Log.d(LOG_TAG, "BootupReceiver constructor");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(LOG_TAG, "Start Tilt Service");
        Intent tiltService = new Intent(context, TiltService.class);
        context.startService(tiltService);
    }
}
