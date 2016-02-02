package kr.dude.newtag.DataCollector;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;

/**
 * Created by madcat on 2/1/16.
 */
public interface DataCollectService {

    /**
     * 서버에 SVM 트레이닝 데이터를 업로드한다.
     * @param filename
     * @param data
     * @return
     */
    @FormUrlEncoded
    @POST("/newTag/upload.php")
    Call<String> uploadData(@Field("filename") String filename, @Field("data") String data);
}
