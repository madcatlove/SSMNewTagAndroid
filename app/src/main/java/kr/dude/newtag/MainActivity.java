package kr.dude.newtag;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

    Button recordStartBtn, recordStopBtn;
    Button playMediaBtn, play_recordBtn;
    Button btnSvmTrain, btnSvmPredict;
    AudioRecorder audioRecorder;
    AudioPlayer audioPlayer;
    AudioController audioController;


    private void initView() {
        recordStartBtn = (Button)findViewById(R.id.btn_startRecord);
        recordStopBtn = (Button) findViewById(R.id.btn_stopRecord);
        playMediaBtn = (Button) findViewById(R.id.btn_playMedia);
        play_recordBtn = (Button) findViewById(R.id.btn_playController);
        btnSvmTrain = (Button) findViewById(R.id.btn_svmtrain);
        btnSvmPredict = (Button) findViewById(R.id.btn_svmpredict);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initView();

        audioRecorder = new AudioRecorder(this);
        audioPlayer = new AudioPlayer(this);
        audioController = new AudioController(this);

        recordStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), " START AUDIO RECORD!! ", Toast.LENGTH_SHORT).show();
                audioRecorder.startRecord();
            }
        });

        recordStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), " STOP AUDIO RECORD!! ", Toast.LENGTH_SHORT).show();
                audioRecorder.stopRecord();
            }
        });

        playMediaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), " PLAY AUDIO!! ", Toast.LENGTH_SHORT).show();

                audioPlayer.playSound(R.raw.full_sample, null);
            }
        });

        play_recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioController.playSoundAndRecord();
            }
        });




        btnSvmTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SVMTrain svmTrain = new SVMTrain();
                try {
                    Toast.makeText(getBaseContext(), " RUN SVM!! ", Toast.LENGTH_SHORT).show();
                    svmTrain.setFileDirPath(Util.getSVMDir());
                    svmTrain.setModelFileName("splice_scale_output.model");
                    svmTrain.loadProblem("splice_scale.txt");
                    svmTrain.doTrain();
                }
                catch(IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, " SVMTRAIN ERROR " + e.getMessage());
                }
            }
        });

        btnSvmPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String modelFileName = Util.getSVMDir() + "splice_scale_output.model";
                String testFileName = Util.getSVMDir() + "splice.t";
                String outputFileName = Util.getSVMDir() + "splice_predict_output.txt";
                SVMPredict svmPredict = new SVMPredict(modelFileName, testFileName, outputFileName);

                try {
                    Log.w(LOG_TAG, " START PREDICT ");
                    svmPredict.doPredict();
                }
                catch(IOException e) {
                    e.printStackTrace();
                }
            }
        });


        // TILT
//        Intent tiltService = new Intent(this, TiltService.class);
//        startService(tiltService);


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
