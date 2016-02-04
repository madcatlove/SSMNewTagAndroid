package kr.dude.newtag;

import android.os.Environment;
import android.util.Log;

/**
 * Created by madcat on 2/1/16.
 */
public class Constant {
    public static String BASE_SERVER_ADDR = "http://my.dude.kr";

    // SD 카드 리턴 없으면 기존 내부저장소
    public static String getSdcardPath() {
        String externalState = Environment.getExternalStorageState();

        if( externalState.equals(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().getAbsolutePath();
        }

        return Environment.getExternalStorageDirectory().toString();
    }
}
