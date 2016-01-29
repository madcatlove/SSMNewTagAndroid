package kr.dude.newtag.TiltManager;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;


public class WifiScanner extends IntentService {

    private static final String LOG_TAG = "WifiScanner";
    private boolean ready = true;

    public WifiScanner() {
        this("WifiScannerIntentService");
    }

    public WifiScanner(String name) {
        super(name);
        Log.i(LOG_TAG, "WifiScanner created!!");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        WifiRequestInfo.requestWifiInformationNewer(this);

        //List<ScanResult> wifiList = wifiManager.getScanResults();
        //Log.v(LOG_TAG, "" + wifiList);
    }
}
