package kr.dude.newtag.Sense;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private SenseListener mSenseListener;

    private Handler mHandler;

    /* (Prediction) 녹음 셋트 */
    private int current = 1;
    private static final int target = 6;

    /* 최종 결과물 (콜백 반환용*/
    Map<String, Integer> result = new HashMap<String, Integer>();


    public static interface AfterPrediction {
        public void afterPrediction(Map<String,Integer> predictList);
    }



    public PredictController(Context context) {
        MODEL_DIR = Constant.getSdcardPath() + "/newTag/";
        TRAINING_DIR = Constant.getSdcardPath() + "/newTag/";
        RECORDED_DIR = Constant.getSdcardPath() + "/newTag/";
        audioController = new AudioController(context);
        mContext = context;

    }


    public void setSenseListener(SenseListener sl) {
        mSenseListener = sl;

        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setAfterPredictionListener(AfterPrediction ap) {
        mAfterPredictionListener = ap;
    }

    public void doPredict() {

        /* 콜백 등록 */
        audioController.setOnCompleteExecution(new AudioController.OnCompleteExecution() {
            @Override
            public void execute(Object obj) {
                Log.i(LOG_TAG, " Complete current :: " + current);
                current++;

                if( current > target ) {
                    while(audioController.isRunning()) {;}
                    _doPredict();
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

    private void _doPredict() {

        /* 파일 이름 */
        final String currentDate = SenseEnvironment.getCurrentDateString();
        final String TRAINING_FILE_NAME = String.format("tempFeature_%s.test", currentDate);
        final String SCALE_FILE_NAME = String.format("tempFeature_%s.test.scale", currentDate);

        /************************* MESSAGE LOG *************************/
        if(mSenseListener != null) {

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(30 , " 분석 파일 생성중... ");
                }
            });

        }


        /* 피처 생성 */
        Log.i(LOG_TAG, " EXTRACT FEATURE ");
        ProductSVMTrainSet prod = new ProductSVMTrainSet(RECORDED_DIR, "+1");
        prod.setSaveFilePath(TRAINING_DIR + TRAINING_FILE_NAME);
        prod.makeTrainSet();


        /* SVM Training && Model 생성 */
        Log.i(LOG_TAG, " SVM TRAINING && MAKE MODEL ");

        try {
            /* 1. 스케일 작업 */
            //SVMScale.scale(TRAINING_DIR + TRAINING_FILE_NAME, TRAINING_DIR + SCALE_FILE_NAME);

            List<String> features = SenseEnvironment.fileReadLine(TRAINING_DIR+TRAINING_FILE_NAME);
            List<File> models = SenseEnvironment.getAllModelFiles(MODEL_DIR);


            /************************* MESSAGE LOG *************************/
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSenseListener.updateProgress(80 , " 결과물 분석중.. ");
                }
            });

            /* 모델 파일을 하나씩 순회 */
//            result = featureVerification(features, models);
            result = featureVerificationKNN(features, models);

        }
        catch(IOException e) {
            Log.w(LOG_TAG, "IOException" + e.getMessage());
            e.printStackTrace();
        }


        /* 웨이브파일 모두 삭제 */
        SenseEnvironment.removeAllWavFiles(RECORDED_DIR);
//        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".test");
//        SenseEnvironment.removeAllFilesWithExtension(RECORDED_DIR, ".scale");


        /************************* MESSAGE LOG *************************/
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSenseListener.updateProgress(100 , "끝");
            }
        });


        /* 콜백이 있다면 콜백실행 */
        if( mAfterPredictionListener != null ) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAfterPredictionListener.afterPrediction(result);
                }
            });
        }


    }

    /**
     * KNN 피처 확인
     * @param features
     * @param models
     * @return
     * @throws IOException
     */
    public Map<String, Integer> featureVerificationKNN(List<String> features, List<File> models) throws IOException {

        KNNPredict predict = new KNNPredict();
        predict.loadModels(MODEL_DIR, models);

        /* 4C2 조합 */
        int[][] comb = {
                {0,1}, {0,2}, {0,3},
                {1,2}, {1,3}, {2,3}
        };

        /* features 압축 */
        List<String> compressedFeatures = new ArrayList<String>();
        for(int i = 0; i < features.size(); i += 2) {
            String first = features.get( i );
            String second = features.get( i+1 );

            String compressedMean = meanFeature(first,second);


            Log.e(LOG_TAG, " Compressed Feature :: " + compressedMean);
            /* 평균된 피처 새로 넣음 */
            compressedFeatures.add(compressedMean);
        }

        HashMap<String,Integer> predictCount = predict.validation(compressedFeatures);

        return predictCount;
    }


    /**
     * features(피처 리스트)에서 하나씩 꺼내서 각각의 모델과 비교해본다
     * @param features
     * @param models
     * @return Map (String,Integer String:modelName, Integer:분류된갯수)
     */
    public Map<String, Integer> featureVerification(List<String> features, List<File> models) throws IOException {

        final String oneFeatureName = "tempFeatureOne.test";
        HashMap<String, Double> predictResult = new HashMap<String, Double>();
        HashMap<String, Integer> predictCount = new HashMap<String, Integer>();

        // 초기화
        for(File f : models) {
            predictCount.put(f.getName(), 0);
        }

        for(String feature : features) {

            /* 피처 하나만 저장 */
            SenseEnvironment.saveFile(TRAINING_DIR + oneFeatureName, feature);

            /* 모델과 비교해본다 */
            for (int i = 0; i < models.size(); i++) {
                String modelName = models.get(i).getName();

                //SVMPredict p = new SVMPredict(MODEL_DIR+modelName, TRAINING_DIR + SCALE_FILE_NAME, TRAINING_DIR + "output.test");
                SVMPredict p = new SVMPredict(MODEL_DIR + modelName, TRAINING_DIR + oneFeatureName, TRAINING_DIR + "output.test");

                p.setPredictProbability(0);
                List<Double> result = p.doPredict();

                /* 값 저장 */
                if (result.size() == 1) {
                    double fSum = result.get(0);
                    predictResult.put(modelName, fSum);
                    Log.i(LOG_TAG, " Model : " + modelName + " :: SUM :: " + fSum);
                }

            }

            /* ///해쉬맵에 저장된 값들중 가장 값이큰(정확한) 모델에 카운트 올려준다 */
            double max = -987654321.0f;
            String selectedModel = "";
            Set<String> p = predictResult.keySet();
            for(String modelName : p) {
                double v = predictResult.get(modelName);
                if( v > max ) {
                    selectedModel = modelName;
                    max = v;
                }
            }

            if(!"".equals(selectedModel)) {
                predictCount.put(selectedModel, predictCount.get(selectedModel) + 1);
            }
            /* 해쉬맵에 저장된 값들중 가장 값이큰(정확한) 모델에 카운트 올려준다/// */



            /* 임시 파일 삭제 */
            SenseEnvironment.removeFile(TRAINING_DIR + oneFeatureName);
        }


        return predictCount;
    }


    /**
     * first, second 피처를 평균내서 String 형태로 반환
     * @param first
     * @param second
     * @return
     */
    private String meanFeature(String first, String second) {
        String[] v_first = first.split(",");
        String[] v_second = second.split(",");

        double[] compressed = new double[first.length()];

        StringBuilder b = new StringBuilder();
        for(int k = 0; k < v_first.length; k++) {
            compressed[k] = ( Double.valueOf(v_first[k]) + Double.valueOf(v_second[k]) ) / 2.0;
            b.append(compressed[k]);
            if( k < v_first.length-1) {
                b.append(",");
            }
        }

        return b.toString();
    }




}
