package kr.dude.newtag.Sense;

/**
 * Created by madcat on 2016. 2. 11..
 */


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private SenseListener mSenseListener;

    private Handler mHandler;


    /* (Training Sense) 녹음 셋트 */
    private int current = 1;
    private static final int target = 15;


    public SenseController(Context context) {
        MODEL_DIR = Constant.getSdcardPath() + "/newTag/";
        TRAINING_DIR = Constant.getSdcardPath() + "/newTag/";
        RECORDED_DIR = Constant.getSdcardPath() + "/newTag/";
        audioController = new AudioController(context);
        mContext = context;

        current = 1;
    }


    public void setSenseListener(SenseListener sl) {
        mSenseListener = sl;

        mHandler = new Handler(Looper.getMainLooper());
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

                    /* MESSAGE LOG */
                    if(mSenseListener != null) {

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                int precent = (int) (((double) current/target) * 30);
                                mSenseListener.updateProgress(precent , " 소리 녹음중 ... " + String.format("%d/%d", current, target));
                            }
                        });

                    }

                    if( current >= target) break;
                }
            }
        });

        t.start();



    }


    private void _doSense() {
        /* 모델번호 획득 */
        int modelNum = SenseEnvironment.getRecentModelNumber() + 1;

        /************************* MESSAGE LOG *************************/
        if(mSenseListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(30, " 트레이닝 파일 생성중.. ");
                }
            });
        }

        /* SVM 피처 생성 */
        Log.i(LOG_TAG, " EXTRACT FEATURE ");
        final String TRAINING_FILE_NAME = String.format("training%d.train", modelNum);
        ProductSVMTrainSet prod = new ProductSVMTrainSet(RECORDED_DIR, "+1");
        prod.setSaveFilePath(TRAINING_DIR + TRAINING_FILE_NAME);

        /* SVM 피처 생성 ( 피처 생성 스레드 하나가 끝날때마다 이 콜백함수를 호출한다 */
        prod.setExtractDoneNotifier(new ProductSVMTrainSet.ExtractDoneNotifier() {

            int threadDone = 0;

            @Override
            public void extractDone(Object obj) {
                final String threadName = (String) obj;
                threadDone++;

                if(mSenseListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mSenseListener.updateProgress(30 + (int)(((double)threadDone/target)*60), String.format("완료.. (%d/%d)", threadDone, target));
                        }
                    });
                }
            }
        });
        prod.makeTrainSet();


        /************************* MESSAGE LOG *************************/
        if(mSenseListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(90, " SVM 모델 생성 시작 ");
                }
            });

        }


        /* 트레이닝셋끼리 값 주고받음. */
//        SenseEnvironment.trainsetMixer(TRAINING_DIR, TRAINING_FILE_NAME);

        /* FOR K-NN algorithm training file을 그냥 모델에 저장 */
        Log.i(LOG_TAG, " Make KNN fileset ");
        final String MODEL_FILE_NAME = String.format("model%d.model", modelNum);
        try {

            /* TRAINING_FILE 을 읽어서 모델에 저장 */
            SenseEnvironment.saveFile(MODEL_DIR + MODEL_FILE_NAME,
                    SenseEnvironment.fileReadString(TRAINING_DIR + TRAINING_FILE_NAME));

            /* 모델번호 증가 */
            SenseEnvironment.writeModelNumber(modelNum);
        } catch(IOException e) {
            e.printStackTrace();
        }



        /* 방금 생성된 피쳐 SVM Training && Model 생성 */
//        Log.i(LOG_TAG, " SVM TRAINING && MAKE MODEL ");
//        final String SCALE_FILE_NAME = String.format("training%d.train.scale", modelNum);
//        final String MODEL_FILE_NAME = String.format("model%d.model", modelNum);
//        try {
//            SVMScale.scale(TRAINING_DIR + TRAINING_FILE_NAME, TRAINING_DIR + SCALE_FILE_NAME);
//            SVMTrain svmTrain = new SVMTrain();
//            svmTrain.setModelFileName(MODEL_DIR + MODEL_FILE_NAME);


//            svmTrain.loadProblem(TRAINING_DIR + SCALE_FILE_NAME);
//            svmTrain.loadProblem(TRAINING_DIR + TRAINING_FILE_NAME);
//            svmTrain.doTrain();

            /* 모델 번호 증가 */
//            SenseEnvironment.writeModelNumber(modelNum);
//        }
//        catch(IOException e) {
//            Log.e(LOG_TAG, "CANNOT_SCALE OR CANNOT_MAKE_MODEL");
//            e.printStackTrace();
//        }




        /************************* MESSAGE LOG *************************/
        if(mSenseListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(90, " 잔업중...(정리..) ");
                }
            });

        }

        /* 이전에 생성된 트레이닝 파일이 변경되었으므로 모델 재생성 */
//        List<File> oldTrainSet = SenseEnvironment.getAllTrainFiles(TRAINING_DIR);
//        for( File oldTrainFile : oldTrainSet) {
//            if( oldTrainFile.getName().equals(TRAINING_FILE_NAME) ) {
//                 방금전에 생성된 파일이므로 패스
//                continue;
//            }
//
//             모델번호 추출해옴
//            final String regex = "^training(\\d+)\\.train$";
//            Pattern p = Pattern.compile(regex);
//            Matcher mat = p.matcher(oldTrainFile.getName());
//
//            if(mat.find()) {
//                int _modelNum = Integer.valueOf( mat.group(1) );
//                try {
//                    String OVERWRITE_MODEL_NAME = String.format("model%d.model", _modelNum);
//
//                    SVMTrain svmTrain = new SVMTrain();
//                    svmTrain.setModelFileName(MODEL_DIR + OVERWRITE_MODEL_NAME);
//                    svmTrain.loadProblem(TRAINING_DIR + oldTrainFile.getName());
//                    svmTrain.doTrain();
//                }
//                catch(IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//
//        }


        /* 웨이브파일 모두 삭제 */
        SenseEnvironment.removeAllWavFiles(RECORDED_DIR);
//        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".train");
//        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".scale");


        /************************* MESSAGE LOG *************************/
        if(mSenseListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(100, " 완료 ");
                }
            });

        }
    }
}
