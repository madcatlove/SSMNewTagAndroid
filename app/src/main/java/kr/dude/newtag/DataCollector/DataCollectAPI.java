package kr.dude.newtag.DataCollector;

import android.util.Log;

import retrofit.Call;
import retrofit.Callback;

/**
 * Created by madcat on 2/1/16.
 */
public class DataCollectAPI extends API {

    DataCollectService service;
    private static final String LOG_TAG = "DataCollectAPI";

    public DataCollectAPI() {
        service = retrofit.create(DataCollectService.class);
    }

    public void sendData(String filename, String data, Callback<String> cb) {
        Log.i(LOG_TAG, "sendData()");
        Call<String> call = service.uploadData(filename, data);
        call.enqueue(cb);
    }

}
