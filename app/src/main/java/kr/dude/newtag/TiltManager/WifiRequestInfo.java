package kr.dude.newtag.TiltManager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by madcat on 1/13/16.
 */

// 와이파이 요청정보를 가지고 있음.
public class WifiRequestInfo {

    private static final String LOG_TAG = "WifiRequestInfo";
    private static long lastRequestTime = 0;
    private static final int WIFI_RENEW_TIME = 600; // 초, 600초후 갱신

    public static void requestWifiInformationNewer(Context ctx) {
        WifiManager wifiManager = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);

        int diffTime = (int) (System.currentTimeMillis() - lastRequestTime) / 1000;
        if( diffTime < WIFI_RENEW_TIME ) { /* 무시 */
            return;
        }

        wifiManager.startScan();
        lastRequestTime = System.currentTimeMillis();
        Log.d(LOG_TAG, "REQUEST WIFI INFO");
    }

}
