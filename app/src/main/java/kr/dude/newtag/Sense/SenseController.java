package kr.dude.newtag.Sense;

/**
 * Created by madcat on 2016. 2. 11..
 */


import android.content.Context;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

import kr.dude.newtag.Audio.AudioController;
import kr.dude.newtag.Constant;
import kr.dude.newtag.SVM.ProductSVMTrainSet;
import kr.dude.newtag.SVM.SVMScale;
import kr.dude.newtag.SVM.SVMTrain;

/**
 * 1. 10개의 음원 파일 재생 및 녹음
 * 2. 10개의 녹음된 음원에서 하나의 트레이닝셋 생성
 * 3. SVM Training && Model 생성
 */
public class SenseController {
    private static final String LOG_TAG = "SenseController";

    private String MODEL_DIR;
    private String RECORDED_DIR;
    private String TRAINING_DIR;
    private AudioController audioController;
    private Context mContext;


    /* 녹음 셋트 */
    private int current = 1;
    private static final int target = 10;

    public SenseController(Context context) {
        MODEL_DIR = Constant.getSdcardPath() + "/newTag/";
        TRAINING_DIR = Constant.getSdcardPath() + "/newTag/";
        RECORDED_DIR = Constant.getSdcardPath() + "/newTag/";
        audioController = new AudioController(context);
        mContext = context;

        current = 1;
    }


    /**
     *
     */
    public void doSense() {

        /* 콜백 등록 */
        audioController.setOnCompleteExecution(new AudioController.OnCompleteExecution() {
            @Override
            public void execute(Object obj) {
                Log.i(LOG_TAG, " Complete current :: " + current);
                current++;

                if( current > target ) {
                    while(audioController.isRunning()) {;}
                    _doSense();
                }
            }
        });

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                /* 녹음 시작 */
                Log.i(LOG_TAG, " START_AUDIO_RECORD ");
                while(true) {
                    if( AudioController.isRunning()) continue;
                    Log.e(LOG_TAG, " >>>>> CURRENT :: " + current);
                    audioController.playSoundAndRecord();

                    if( current >= target) break;
                }
            }
        });

        t.start();



    }


    private void _doSense() {
        /* 모델번호 획득 */
        int modelNum = SenseEnvironment.getRecentModelNumber() + 1;


        /* SVM 피처 생성 */
        Log.i(LOG_TAG, " EXTRACT SVM FEATURE ");
        final String TRAINING_FILE_NAME = String.format("training%d.train", modelNum);
        ProductSVMTrainSet prod = new ProductSVMTrainSet(RECORDED_DIR, "+1");
        prod.setSaveFilePath(TRAINING_DIR + TRAINING_FILE_NAME);
        prod.makeTrainSet();


        /* SVM Training && Model 생성 */
        Log.i(LOG_TAG, " SVM TRAINING && MAKE MODEL ");
        final String SCALE_FILE_NAME = String.format("training%d.train.scale", modelNum);
        final String MODEL_FILE_NAME = String.format("model%d.model", modelNum);
        try {
//            SVMScale.scale(TRAINING_DIR + TRAINING_FILE_NAME, TRAINING_DIR + SCALE_FILE_NAME);
            SVMTrain svmTrain = new SVMTrain();
            svmTrain.setModelFileName(MODEL_DIR + MODEL_FILE_NAME);
//            svmTrain.loadProblem(TRAINING_DIR + SCALE_FILE_NAME);
            svmTrain.loadProblem(TRAINING_DIR + TRAINING_FILE_NAME);
            svmTrain.doTrain();

            /* 모델 번호 증가 */
            SenseEnvironment.writeModelNumber(modelNum);
        }
        catch(IOException e) {
            Log.e(LOG_TAG, "CANNOT_SCALE OR CANNOT_MAKE_MODEL");
            e.printStackTrace();
        }


        /* 웨이브파일 모두 삭제 */
        SenseEnvironment.removeAllWavFiles(RECORDED_DIR);
        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".train");
        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".scale");
    }
}
