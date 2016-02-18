package kr.dude.newtag.SVM;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import kr.dude.newtag.AudioAnalyzer.ExtractException;
import kr.dude.newtag.AudioAnalyzer.FeatureExtractor2;

/**
 * Created by madcat on 2016. 2. 11..
 *
 * 주어진 디렉토리에서 .wav 파일을 읽어 트레이닝 가능한 피처 셋으로 만든다.
 */
public class ProductSVMTrainSet {

    private static final String LOG_TAG = "ProductSVMTrainSet";
    private File dir = null;
    private String result = "";
    private String label;
    private String saveFilePath;
    private static final int CONCURRENCY_THREAD = 6; // 동시에 돌릴 스레드 갯수

    /**
     * Constructor
     * @param dirPath .wav 파일 위치
     * @param label SVM Label
     */
    public ProductSVMTrainSet(String dirPath, String label) {
        this.dir = new File(dirPath);
        this.label = label;
        if( !dir.exists() || !dir.isDirectory() ) {
            throw new RuntimeException("No directory");
        }
    }

    /**
     * SVM 피처 추출후 파일로 내림
     * 저장되는 위치는 @see setSaveFilePath 참조
     */
    public void makeTrainSet()  {

        File[] fileList = dir.listFiles();

        int cnt=0;
        Queue<Thread> threadList = new LinkedBlockingQueue<Thread>();

        for(File file : fileList) {


			/* Thread 시작하고 큐에 추가 */
            if( file.isFile()) {
                String fileName = file.getName();
                if( fileName.endsWith(".wav") ) {
                    String fullPath = dir.getPath() + "/" + fileName;
                    Thread t = new DoExtract(fullPath);
                    t.setPriority(Thread.MAX_PRIORITY);

                    threadList.add(t);
                    t.start();
                }
            }

			/* 큐의 크기가 CONCURRENCY_THREAD 이상이면 큐에서 꺼내면서 조인 시작 */
            if( threadList.size() >= CONCURRENCY_THREAD) {
                while( !threadList.isEmpty()) {
                    Thread t = threadList.poll();
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

		/* 큐에 남아있다면.. */
        while( !threadList.isEmpty() ) {
            Thread t = threadList.poll();
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        if( saveFilePath != null) {
            File sFile = new File(saveFilePath);
            try {
                PrintWriter pw = new PrintWriter(sFile);
                pw.write(result);
                pw.flush();
                pw.close();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new RuntimeException("NO_SAVE_FILE_PATH!!");
        }

    }


    /**
     * 저장위치 정함
     * @param path 트레이닝 파일이 저장될곳 + 파일이름
     */
    public void setSaveFilePath(String path) {
        saveFilePath = path;
    }

    /* result에 스트링 이어붙임 */
    private synchronized void stringAppend(String data) {
        result += data + "\n";
    }


    /**
     * 추출 역할
     */
    private class DoExtract extends Thread {

        private String filePath;
        public DoExtract(String filePath) {
            this.filePath = filePath;
        }

        @Override
        public void run() {

            Log.d(LOG_TAG, " Run thread :: " + filePath);
            FeatureExtractor2 fe = new FeatureExtractor2(filePath, label);
            try {
                String data = fe.getSvmFeature(2);
                stringAppend(data);
            }
            catch(IOException | ExtractException e) {
                e.printStackTrace();
            }
        }



    }
}
