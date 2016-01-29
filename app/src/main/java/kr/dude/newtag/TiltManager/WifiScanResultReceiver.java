package kr.dude.newtag.TiltManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiScanResultReceiver extends BroadcastReceiver {

    private static final String LOG_TAG = "WifiScanResultReceiver";

    public WifiScanResultReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) ) {
            Log.d(LOG_TAG, "111111");
        }


    }
}
