package kr.dude.newtag.Audio;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import kr.dude.newtag.AudioAnalyzer.FeatureExtractor;
import kr.dude.newtag.R;

/**
 * Created by madcat on 1/17/16.
 */
public class AudioController {

    private static final String LOG_TAG = "AudioController";
    private final Context mContext;
    private final AudioRecorder audioRecorder;
    private final AudioPlayer audioPlayer;
    private final long DELAY_TIME = 1000;
    private final AudioPlayer.CompleteCallback audioCompleteCallback = new AudioOnComplete();
    private final AudioRecorder.CompleteCallback recorderCompleteCallback = new RecorderOnComplete();

    public AudioController(Context context) {
        mContext = context;
        audioPlayer = new AudioPlayer(context);
        audioRecorder = new AudioRecorder(context);

        audioRecorder.setRecorderOnComplete(recorderCompleteCallback);
    }

    public void playSoundAndRecord() {

        // 레코딩 시작
        audioRecorder.startRecord();

        // 정해진 시각뒤에 소리 재생
        Handler handler = new Handler();
        Runnable playSoundThread = new Runnable() {
            @Override
            public void run() {
                playSound();
            }
        };

        handler.postDelayed(playSoundThread, DELAY_TIME);

    }

    private void playSound() {
        if( audioPlayer != null) {
            audioPlayer.playSound(R.raw.full_sample, audioCompleteCallback);
        }
    }


    /* 오디오 재생이 끝났을때 실행할 콜백 */
    private class AudioOnComplete implements AudioPlayer.CompleteCallback {
        @Override
        public void afterExecution() {
            Log.d(LOG_TAG, "Player stopped. stop recording");

            // DELAY 시간 이후 종료
            Runnable stopSoundThread = new Runnable() {
                @Override
                public void run() {
                    audioRecorder.stopRecord();
                    Toast.makeText(mContext, " COMPLETE!!! ", Toast.LENGTH_SHORT).show();
                }
            };

            Handler handler = new Handler();
            handler.postDelayed(stopSoundThread, DELAY_TIME);

        }
    }


    /* 오디오 녹음이 끝났을때 실행할 콜백 */
    private class RecorderOnComplete implements AudioRecorder.CompleteCallback {
        @Override
        public void executeTaskOnRecorderStopped(String filePath) {

            final String p = filePath;
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        FeatureExtractor fe = new FeatureExtractor();
                        String svmString = fe.getSvmFeature(p);
                        Log.e(LOG_TAG, svmString);

                    }
                    catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            Log.i(LOG_TAG, " START FEATURE_EXTRACTOR :: filePath : " + p );
            t.start();

        }
    }
}
