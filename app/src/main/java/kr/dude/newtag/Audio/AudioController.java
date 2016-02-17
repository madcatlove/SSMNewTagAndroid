package kr.dude.newtag.Audio;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import kr.dude.newtag.AudioAnalyzer.FeatureExtractor;
import kr.dude.newtag.DataCollector.DataCollectAPI;
import kr.dude.newtag.R;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Created by madcat on 1/17/16.
 */
public class AudioController {

    private static final String LOG_TAG = "AudioController";
    private final Context mContext;
    private final AudioRecorder audioRecorder;
    private final AudioPlayer audioPlayer;
    private final long DELAY_TIME = 200; // ms
    private final AudioPlayer.CompleteCallback audioCompleteCallback = new AudioOnComplete();
    private final AudioRecorder.CompleteCallback recorderCompleteCallback = new RecorderOnComplete();
    private static boolean isRunning = false;
    private OnCompleteExecution onCompleteExecution = null;

    /* 오디오 컨트롤러 작업이 모두 끝나고 실행할 작업 */
    public static interface OnCompleteExecution {
        public void execute(Object obj);
    }

    public AudioController(Context context) {
        mContext = context;
        audioPlayer = new AudioPlayer(context);
        audioRecorder = new AudioRecorder(context);
        audioRecorder.setRecorderOnComplete(recorderCompleteCallback);
    }

    /**
     * 소리 재생과 녹음 시작
     */
    public void playSoundAndRecord() {

        if( isRunning() ) {
            Log.e(LOG_TAG, " Already running ");
            return;
        }

        setRunningState(true);

        // 레코딩 시작
        audioRecorder.startRecord();

        // 정해진 시각뒤에 소리 재생
        Handler handler = new Handler(Looper.getMainLooper());
        Runnable playSoundThread = new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, " Execute playSound() ");
                playSound();
            }
        };

        handler.postDelayed(playSoundThread, DELAY_TIME);



    }

    /**
     * 등록된 오디오 플레이어로 소리 재생을 시작함
     */
    private void playSound() {
        if( audioPlayer != null) {
            audioPlayer.playSound(R.raw.input_signal, audioCompleteCallback);

        }
    }


    /* 오디오 컨트롤러를 사용할수있는지? */
    public static synchronized boolean isRunning() {
        return isRunning;
    }

    /* 오디오 컨트롤러 상태 바꿈 */
    public static synchronized void setRunningState(boolean state) {
        isRunning = state;
    }

    public void setOnCompleteExecution(OnCompleteExecution onCompleteExecution) {
        this.onCompleteExecution = onCompleteExecution;
    }


    /*******************************************************************
     ********************* INNER CLASS *********************************
     * 오디오 재생이 끝났을때 실행할 콜백
     * 1. 재생이 끝났을때 DELAY_TIME 후에 녹음기 기능을 끈다.
     * 2. 컨트롤러 잠금 해제
     *******************************************************************
     */
    private class AudioOnComplete implements AudioPlayer.CompleteCallback {
        @Override
        public void afterExecution() {
            Log.d(LOG_TAG, "Player stopped. stop recording");

            // DELAY 시간 이후 종료
            Runnable stopSoundThread = new Runnable() {
                @Override
                public void run() {
                    audioRecorder.stopRecord();

                }
            };

            Handler handler = new Handler();
            handler.postDelayed(stopSoundThread, DELAY_TIME);


        }
    }


    /*******************************************************************
     ********************* INNER CLASS *********************************
     * 1. 오디오 녹음이 끝났을때 실행할 콜백
     * 2. 모든 작업이 끝나고 onCompleteExecution 콜백 실행필요
     *******************************************************************
     */
    private class RecorderOnComplete implements AudioRecorder.CompleteCallback {
        @Override
        public void executeTaskOnRecorderStopped(String filePath) {

            /* SVM 피쳐 생성 */
            final String p = filePath;

            setRunningState(false);

            /* onCompleteExecution 콜백 실행 */
            if (onCompleteExecution != null) {
                onCompleteExecution.execute(null);
            }

            Log.i(LOG_TAG, " START FEATURE_EXTRACTOR :: filePath : " + p);

        }
    }







}
