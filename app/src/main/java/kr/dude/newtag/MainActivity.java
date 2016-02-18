package kr.dude.newtag;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import kr.dude.newtag.Audio.AudioController;
import kr.dude.newtag.Audio.AudioPlayer;
import kr.dude.newtag.Audio.AudioRecorder;
import kr.dude.newtag.SVM.SVMPredict;
import kr.dude.newtag.SVM.SVMTrain;
import kr.dude.newtag.SVM.Util;
import kr.dude.newtag.Sense.PredictController;
import kr.dude.newtag.Sense.SenseController;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";


    Button btnSvmTrain, btnSvmPredict;
    TextView status_view;

    AudioRecorder audioRecorder;
    AudioPlayer audioPlayer;
    AudioController audioController;

    int target = 10;
    int current = 0;
    PowerManager.WakeLock wakeLock = null;
    PowerManager pm = null;

    ArcProgressDialog progressBar;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            status_view.setText(" CURRENT RECORDED :: " + current);
        }
    };


    private void initView() {
        btnSvmTrain = (Button) findViewById(R.id.btn_svm_train);
        btnSvmPredict = (Button) findViewById(R.id.btn_svm_predict);

//        progressBar = new ProgressDialog(this);
        progressBar = new ArcProgressDialog(this);
        progressBar.setCancelable(false);
//        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//        progressBar.setProgressStyle(0);
//        progressBar.setMax(100);
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


        /*****************************************************************
         * SVM TRAIN
         *****************************************************************/
        btnSvmTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SenseController s = new SenseController(MainActivity.this);

                s.setSenseListener(new SenseController.SenseListener() {
                    @Override
                    public void updateProgress(Integer precent, String message) {
                        if(precent == 100) {
                            progressBar.dismiss();
                            return;
                        }
                        progressBar.setPercent(precent);
                        progressBar.setMessage(message);
//                        progressBar.setProgress(precent);
//                        progressBar.setMessage(message);

                    }
                });

                progressBar.show();
                s.doSense();
            }
        });


        /*****************************************************************
         * SVM PREDICTION
         *****************************************************************/
        btnSvmPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, " 대기중.... ", Toast.LENGTH_SHORT).show();


                // -- 2초후 시작 --
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        PredictController p = new PredictController(MainActivity.this);
                        p.setAfterPredictionListener(new PredictController.AfterPrediction() {
                            @Override
                            public void afterPrediction(Map<String, Double> predictList) {

                                StringBuffer result = new StringBuffer();

                                for (String modelName : predictList.keySet()) {
                                    result.append(String.format("%s : %f\n", modelName, predictList.get(modelName)));
                                }

                                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();     //닫기
                                    }
                                });

                                alert.setTitle(" :=: PREDICTION RESULT :=: ");
                                alert.setMessage(result.toString());
                                alert.show();

                            }
                        });

                        p.doPredict();
                    }
                }, 2000);


            }
        });


        // TILT
//        Intent tiltService = new Intent(this, TiltService.class);
//        startService(tiltService);


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (wakeLock != null) {
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

        if (wakeLock != null) {
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
