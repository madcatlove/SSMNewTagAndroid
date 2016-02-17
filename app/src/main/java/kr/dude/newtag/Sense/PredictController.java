package kr.dude.newtag.Sense;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.dude.newtag.Audio.AudioController;
import kr.dude.newtag.Constant;
import kr.dude.newtag.SVM.ProductSVMTrainSet;
import kr.dude.newtag.SVM.SVMPredict;
import kr.dude.newtag.SVM.SVMScale;
import kr.dude.newtag.SVM.SVMTrain;

/**
 * Created by madcat on 2016. 2. 11..
 */
public class PredictController {

    private static final String LOG_TAG = "PredictController";

    private String MODEL_DIR;
    private String RECORDED_DIR;
    private String TRAINING_DIR;
    private AudioController audioController;
    private Context mContext;
    private AfterPrediction mAfterPredictionListener;

    public interface AfterPrediction {
        public void afterPrediction(Map<String,Double> predictList);
    }


    public PredictController(Context context) {
        MODEL_DIR = Constant.getSdcardPath() + "/newTag/";
        TRAINING_DIR = Constant.getSdcardPath() + "/newTag/";
        RECORDED_DIR = Constant.getSdcardPath() + "/newTag/";
        audioController = new AudioController(context);
        mContext = context;

    }

    public void setAfterPredictionListener(AfterPrediction ap) {
        mAfterPredictionListener = ap;
    }

    public void doPredict() {

        /* 콜백 등록 */
        audioController.setOnCompleteExecution(new AudioController.OnCompleteExecution() {
            @Override
            public void execute(Object obj) {
                _doPredict();
            }
        });

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                /* 녹음 시작 */
                audioController.playSoundAndRecord();
            }
        });

        t.start();

    }

    private void _doPredict() {

        Map<String, Double> predictResult = new HashMap<String,Double>();

        /* SVM 피처 생성 */
        Log.i(LOG_TAG, " EXTRACT SVM FEATURE ");
        final String TRAINING_FILE_NAME = String.format("tempFeature.test");
        ProductSVMTrainSet prod = new ProductSVMTrainSet(RECORDED_DIR, "+1");
        prod.setSaveFilePath(TRAINING_DIR + TRAINING_FILE_NAME);
        prod.makeTrainSet();


        /* SVM Training && Model 생성 */
        Log.i(LOG_TAG, " SVM TRAINING && MAKE MODEL ");
        final String SCALE_FILE_NAME = "tempFeature.test.scale";

        try {
            /* 1. 스케일 작업 */
//            SVMScale.scale(TRAINING_DIR + TRAINING_FILE_NAME, TRAINING_DIR + SCALE_FILE_NAME);

            /* 모델 파일을 하나씩 순회하면서 근접값 출력 */
            List<File> models = SenseEnvironment.getAllModelFiles(MODEL_DIR);

            for(int i = 0; i < models.size(); i++) {
                String modelName = models.get(i).getName();
//                SVMPredict p = new SVMPredict(MODEL_DIR+modelName, TRAINING_DIR + SCALE_FILE_NAME, TRAINING_DIR + "output.test");
                SVMPredict p = new SVMPredict(MODEL_DIR+modelName, TRAINING_DIR + TRAINING_FILE_NAME, TRAINING_DIR + "output.test");
                p.setPredictProbability(0);
                List<Double> result = p.doPredict();

                if( result.size() == 1) {
                    double fSum = result.get(0);
                    predictResult.put(modelName, fSum);
                    Log.i(LOG_TAG, " Model : " + modelName + " :: SUM :: " + fSum);
                }
            }

        }
        catch(IOException e) {
            Log.e(LOG_TAG, "CANNOT_SCALE");
            e.printStackTrace();
        }


        /* 웨이브파일 모두 삭제 */
        SenseEnvironment.removeAllWavFiles(RECORDED_DIR);
        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".test");
        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".scale");

        /* 콜백이 있다면 콜백실행 */
        if( mAfterPredictionListener != null ) {
            Looper.prepare();
            mAfterPredictionListener.afterPrediction(predictResult);
            Looper.loop();
        }

//        Looper.prepare();
//        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
//        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();     //닫기
//            }
//        });
//        alert.setMessage(sum_arr);
//        alert.show();
//        Looper.loop();

    }
}
