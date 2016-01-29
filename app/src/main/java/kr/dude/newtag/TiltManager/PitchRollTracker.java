package kr.dude.newtag.TiltManager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by madcat on 1/13/16.
 */
public class PitchRollTracker {

    private static final String LOG_TAG = "PitchRollTracker";

    // 허용할 차이값
    public static final int THRESHOLD_PITCH = 2;
    public static final int THRESHOLD_ROLL = 2;

    private static final int TRACKING_SECONDS = 5000; // 트래킹 할 시간(ms)

    private static final PitchRollTracker instance = new PitchRollTracker();

    private List<PitchRoll> items;


    private PitchRollTracker() {
        items =  Collections.synchronizedList(new ArrayList<PitchRoll>());
    }


    public void addItem(Context context, PitchRoll data) {

        final int itemSize = items.size();

        if( itemSize == 0 && !isFull()) {
            items.add(data);
            return;
        }

        // 이전값과 오차가 THRESHOLD 이상 난다면 취소
        PitchRoll lastItem = items.get( itemSize -1 );
        int diffPitch = Math.abs(data.getPitch() - lastItem.getPitch());
        int diffRoll = Math.abs( data.getRoll() - lastItem.getRoll() );

        if( diffPitch > THRESHOLD_PITCH || diffRoll > THRESHOLD_ROLL ) {
            Log.e(LOG_TAG, " IS NOT TILT !! CANCEL ALL DATA ");
            items.clear();
            return;
        }

        // 풀이 아니라면 데이터 추가
        if(!isFull()) {
            items.add(data);
        }
        // 풀이면 다 비우고 WIFI 검색 시작
        else {
            Log.i(LOG_TAG, " IS TILT !!! NEXT STEP !! ");
            Log.w(LOG_TAG, " first " + items.get(0).getCurrentTime() + " / last " + lastItem.getCurrentTime());
            items.clear();

            doFinalStep(context);
        }


    }

    private void doFinalStep(Context context) {
        Intent wifiScannerIntent = new Intent(context, WifiScanner.class);
        context.startService(wifiScannerIntent);
    }

    private boolean isFull() {
        final int itemSize = items.size();

        if( itemSize > 0 ) {
            PitchRoll lastItem = items.get(  itemSize -1 );
            return lastItem.getCurrentTime() - items.get(0).getCurrentTime() >= TRACKING_SECONDS;
        }

        return false;
    }


    public static PitchRollTracker getInstance() {
        return instance;
    }
}
