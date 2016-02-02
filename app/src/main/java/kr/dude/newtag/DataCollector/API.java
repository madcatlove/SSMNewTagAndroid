package kr.dude.newtag.DataCollector;

import kr.dude.newtag.Constant;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Created by madcat on 2/1/16.
 */
public class API {

    protected Retrofit retrofit;

    public API() {
        retrofit = new Retrofit.Builder()
                .baseUrl(Constant.BASE_SERVER_ADDR)
                .addConverterFactory(new StringConverter())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
