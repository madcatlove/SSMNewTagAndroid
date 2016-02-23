package kr.dude.newtag.Sense;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import kr.dude.newtag.Constant;

/**
 * Created by madcat on 2016. 2. 11..
 */
public class SenseEnvironment {

    private static final String CONFIG_FILE_PATH = Constant.getSdcardPath() + "/newTag/sense_env.config";
    private static final String PREDICT_LOG_FILE_PATH = Constant.getSdcardPath() + "/newTag/sense_predict.log";

    /* 모든 path내 웨이브파일 삭제 */
    public static void removeAllWavFiles(String path) {
        File dir = new File(path);

        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(".wav")) {
                f.delete();
            }
        }
    }

    /* 모든 path내 해당 확장자 삭제 */
    public static void removeAllFilesWithExtension(String path, String ext) {
        File dir = new File(path);

        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(ext)) {
                f.delete();
            }
        }
    }

    /* path내 model 파일 리스트 반환 */
    public static List<File> getAllModelFiles(String path) {
        ArrayList<File> a = new ArrayList<File>();

        File dir = new File(path);
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(".model")) {
                a.add(f);
            }
        }

        return a;
    }


    /* path내 train 파일 리스트 반환 */
    public static List<File> getAllTrainFiles(String path) {
        ArrayList<File> a = new ArrayList<File>();

        File dir = new File(path);
        File[] fileList = dir.listFiles();
        for (File f : fileList) {
            if (f.isFile() && f.getName().endsWith(".train")) {
                a.add(f);
            }
        }

        return a;
    }


    /* 가장 최근 모델 번호를 가져옴 */
    public static int getRecentModelNumber() {
        try {
            File f = new File(CONFIG_FILE_PATH);

            if (!f.exists()) {
                PrintWriter pw = new PrintWriter(CONFIG_FILE_PATH);
                pw.print("0");
                pw.flush();
                pw.close();
            }


            BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE_PATH));
            String modelNum = br.readLine();

            return Integer.valueOf(modelNum);
        }
        catch(IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /* 모델 번호 기록 */
    public static void writeModelNumber(int modelNum) {
        try {
            File f = new File(CONFIG_FILE_PATH);

            PrintWriter pw = new PrintWriter(CONFIG_FILE_PATH);

            if (!f.exists()) {
                pw.print("0");
                pw.flush();
                pw.close();
                return;
            }

            pw.print(String.valueOf(modelNum));
            pw.flush();
            pw.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    /* Model 서로 쓰레기값 교환 */
    public static void trainsetMixer(String dirPath, String targetFile) {
        TrainsetMixer tm = new TrainsetMixer();
        tm.trainsetMix(dirPath, targetFile);
    }

    /* YYYY-mm-dd-hh-ii-ss */
    public static String getCurrentDateString() {
        String m = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss").format(new Date());

        return m;
    }


    /* Prediction 결과값 로깅 */
    public static void writePredictionLog(Map<String, Double> predictResult) throws IOException {

        File f = new File(PREDICT_LOG_FILE_PATH);
        PrintWriter pw = new PrintWriter(new FileOutputStream(f, true));

        pw.println("--------------------------------------------");
        pw.println("Logged Date : " + getCurrentDateString());
        pw.println("--------------------------------------------");

        Set<String> models = predictResult.keySet();
        for(String model : models) {
            pw.println(
                    String.format("Model name : %s \t\t Predicted : %.15f", model, predictResult.get(model))
            );
        }
        pw.println("");

        pw.flush();
        pw.close();

    }

    /**
     * 주어진 파일을 한줄씩 읽어서 리스트로 반환한다
     * @param filePath ( 파일의 경로(이름포함) )
     * @return String list
     * @throws IOException
     */
    public static List<String> fileReadLine(String filePath) throws IOException {
        ArrayList<String> features = new ArrayList<String>();

        BufferedReader b = new BufferedReader(new FileReader(filePath));
        String line;

        while( (line = b.readLine()) != null) {
            if( line.length() > 5 ) {
                features.add(line);
            }
        }

        b.close();

        return features;
    }


    /**
     * 주어진 파일을 String 형태로 반환
     * @param filePath ( 파일의 경로(이름포함) )
     * @return String
     * @throws IOException
     */
    public static String fileReadString(String filePath) throws IOException {
        List<String> data = fileReadLine(filePath);

        StringBuilder b = new StringBuilder();
        for(String s : data) {
            b.append(s + "\n");
        }

        return b.toString();
    }

    /**
     * 파일을 저장한다
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void saveFile(String filePath, String content) throws IOException {
        File f = new File(filePath);
        PrintWriter pw = new PrintWriter(f);

        pw.print(content);
        pw.flush();
        pw.close();

    }

    /**
     * 파일을 저장한다 (Append mode)
     * @param filePath
     * @param content
     * @throws IOException
     */
    public static void saveFileAppend(String filePath, String content) throws IOException {
        PrintWriter p = new PrintWriter(new FileOutputStream(filePath, true));
        p.print(content);
        p.flush();
        p.close();
    }

    /**
     * 파일을 삭제한다
     * @param filePath
     */
    public static void removeFile(String filePath) {

        File f = new File(filePath);
        if( f.exists() ) {
            f.delete();
        }

    }


}
