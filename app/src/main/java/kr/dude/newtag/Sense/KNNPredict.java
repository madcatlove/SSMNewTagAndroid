package kr.dude.newtag.Sense;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Created by madcat on 2016. 2. 23..
 */
public class KNNPredict {

    /** Structure
        model1 => idx0( [1,2,3,4,5] ) idx1( [3,4,5,6,7] ) ....
     */
    private HashMap<String, ArrayList<ArrayList<Double>>> modelData;
    private static final String LOG_TAG = "KNNPredict";

    public KNNPredict() {
        modelData = new HashMap<String, ArrayList< ArrayList<Double> > >();
    }

    public void loadModels(String modelPath, List<File> models) throws IOException {

        for(File file : models) {
            Log.i(LOG_TAG, "Try to load model " + file.getName());
            loadModel(modelPath+file.getName(), file.getName());
        }

    }

    private void loadModel(String modelPath, String modelName) throws IOException {

        List<String> modelContents = SenseEnvironment.fileReadLine(modelPath);

        /*** INIT ***/
        if(!modelData.containsKey(modelName)) {
            modelData.put(modelName, new ArrayList<ArrayList<Double>>() );
        }

        /*** 백터 추가 시작 ***/
        for(String vector : modelContents) {
            ArrayList<Double> tempResult = new ArrayList<Double>();
            String[] v = vector.split(",");



            for(int i = 0; i < v.length; i++) {
                tempResult.add( Double.valueOf(v[i]) );
            }

            /* 모델 데이터에 추가 */
            Log.d(LOG_TAG, "loadModel. modelSize : " + tempResult.size());
            modelData.get(modelName).add(tempResult);
        }

    }

    /**
     * 백터 리스트가 주어질때 각각의 모델과 비교하여 거리가 가장짧은 모델을 찾아낸다
     * @param vectors (백터 리스트)
     */
    public HashMap<String, Integer> validation(List<String> vectors) {

        /* 최종 비교 결과 데이터 */
        ArrayList<KNNDistance> distances = new ArrayList<KNNDistance>();

        /* 결과 카운팅 */
        HashMap<String,Integer> count = new HashMap<String,Integer>();
        for(String modelName : getModelSets()) {
            count.put(modelName, 0);
        }


        for(String vector : vectors) {

            /** 1. 초기화 */
            String[] v = vector.split(",");
            ArrayList<Double> tempResult = new ArrayList<Double>();
            distances = new ArrayList<KNNDistance>();

            for(int i = 0; i < v.length; i++) {
                tempResult.add(Double.valueOf(v[i]));
            }

            /* tempResult과 로딩된 모델들과 전부 비교 */
            Set<String> modelSets = getModelSets();

            /** 2. 모델과 전부비교하여 distances 에 저장 */
            for(String modelName : modelSets) {
                ArrayList<ArrayList<Double>> vectorData = modelData.get(modelName);


                /* oVector (하나의 백터) */
                for(ArrayList<Double> oVector : vectorData) {
                    double distance = 0.0;

                    /* 유클라디안 거리 */
//                    for(int i = 0; i < oVector.size(); i++) {
                        // (a-b)^2
//                        distance += (tempResult.get(i) - oVector.get(i)) * (tempResult.get(i) - oVector.get(i));
//                    }
//                    distance = Math.sqrt(distance);

                    /* Minkowski P = 3 */
                    for(int i = 0; i < oVector.size(); i++) {
                        double r = Math.abs(tempResult.get(i) - oVector.get(i));
                        r = Math.pow(r, 3);
                        distance += r;
                    }
                    distance = Math.pow(distance, 1.0/3.0);

                    // model 이름과함께 저장
                    distances.add( new KNNDistance(modelName, distance));
                    Log.i(LOG_TAG, "Save distance data model name : " + modelName + " distance : " + distance);
                }

            }

            /** 3. 모델별 거리 정렬 */
            Collections.sort(distances, new Comparator<KNNDistance>() {
                @Override
                public int compare(KNNDistance lhs, KNNDistance rhs) {
                    double r = lhs.result - rhs.result;
                    if( r < 0 ) return -1;
                    else if(r > 0) return 1;
                    else return 0;
                }
            });

            /** n개 추출 */
            final int n = 10;
            for(int i = 0; i < n; i++) {
                if( i < distances.size() ) {
                    count.put(distances.get(i).modelName,
                            count.get(distances.get(i).modelName) + 1
                    );
                }
            }


        }


        return count;
    }


    /**
     *
     * @return
     */
    private Set<String> getModelSets() {
        return modelData.keySet();
    }


    /**
     * KNNDistance 저장 클래스.
     */
    private static class KNNDistance {
        final String modelName;
        final Double result;

        public KNNDistance(String modelName, Double result) {
            this.modelName = modelName;
            this.result = result;
        }
    }
}

