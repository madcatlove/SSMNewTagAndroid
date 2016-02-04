package kr.dude.newtag.Audio;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * Created by madcat on 1/17/16.
 */
public class AudioPlayer {

    private static final String LOG_TAG = "AudioPlayer";
    private Context mContext;

    public interface CompleteCallback {
        public void afterExecution();
    }

    public AudioPlayer(Context context) {
        mContext = context;
    }

    public void playSound(int rawId, final CompleteCallback callback) {
        MediaPlayer mediaPlayer = MediaPlayer.create(mContext, rawId);
        mediaPlayer.start();

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {

                if( mediaPlayer != null) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }

                if( callback != null) {
                    callback.afterExecution();
                }
            }
        });
    }

}
