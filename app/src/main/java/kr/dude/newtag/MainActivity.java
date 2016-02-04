package kr.dude.newtag;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import kr.dude.newtag.Audio.AudioController;
import kr.dude.newtag.Audio.AudioPlayer;
import kr.dude.newtag.Audio.AudioRecorder;
import kr.dude.newtag.SVM.SVMPredict;
import kr.dude.newtag.SVM.SVMTrain;
import kr.dude.newtag.SVM.Util;

public class MainActivity extends AppCompatActivity  {

    private static final String LOG_TAG = "MainActivity";


    Button play_recordBtn;
    Button stopBtn;
    TextView status_view;

    AudioRecorder audioRecorder;
    AudioPlayer audioPlayer;
    AudioController audioController;

    int target = 100;
    int current = 0;
    PowerManager.WakeLock wakeLock = null;
    PowerManager pm = null;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            status_view.setText(" CURRENT RECORDED :: " + current);
        }
    };



    private void initView() {
        play_recordBtn = (Button) findViewById(R.id.btn_playController);
        status_view = (TextView) findViewById(R.id.status_view);
        stopBtn = (Button) findViewById(R.id.btn_stop);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();


        // wake lock
        pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "MY_WAKELOCK");


        audioController = new AudioController(this);

        play_recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Thread t =  new Thread(new Runnable() {

                    @Override
                    public void run() {

                        /* 콜백 등록 */
                        audioController.setOnCompleteExecution(new AudioController.OnCompleteExecution() {
                            @Override
                            public void execute(Object obj) {
                                Log.i(LOG_TAG, " Complete current :: " + current);
                                current++;

                                handler.sendEmptyMessage(0);
                            }
                        });

                        /* 녹음 시작 */
                        while(true) {
                            if( AudioController.isRunning()) continue;
                            audioController.playSoundAndRecord();

                            if( current >= target) break;
                        }
                    }
                });

                t.setPriority(Thread.MAX_PRIORITY);
                t.start();


            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                target = -1;
            }
        });


        // TILT
//        Intent tiltService = new Intent(this, TiltService.class);
//        startService(tiltService);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if(wakeLock != null) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if( wakeLock != null) {
            wakeLock.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
