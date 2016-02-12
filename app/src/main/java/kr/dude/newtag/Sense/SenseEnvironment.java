package kr.dude.newtag.Sense;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import kr.dude.newtag.Constant;

/**
 * Created by madcat on 2016. 2. 11..
 */
public class SenseEnvironment {

    private static final String CONFIG_FILE_PATH = Constant.getSdcardPath() + "/newTag/sense_env.config";

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




}
