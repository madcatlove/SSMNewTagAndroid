package kr.dude.newtag.Sense;

/**
 * Created by madcat on 2016. 2. 13..
 */

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class TrainsetMixer {
    private static final String LOG_TAG ="TrainsetMixer";
    private static final String TRAINFILE_EXTENSION = "train";

    public void trainsetMix(String dirPath, String newFileName) {
        ArrayList<File> fileList = getFileList(dirPath);
        File newFile = null;
        for (Iterator<File> iter = fileList.iterator(); iter.hasNext(); ) {
            File tempFile = iter.next();
            if (tempFile.isFile()) {
                String tempFileName = tempFile.getName();
                if (tempFileName.equalsIgnoreCase(newFileName)) {
                    newFile = tempFile;
                    iter.remove();
                }
            }
        }

        appendToOthers(fileList, newFile);
        appendToOne(fileList, newFile);
    }

    private void appendToOne(ArrayList<File> fileList, File newFile) {
        try {
            FileWriter fw = new FileWriter(newFile, true);
            for (File tempFile : fileList) {
                if (tempFile.isFile()) {
                    ArrayList<String> tempContent = file2stringList(tempFile);
                    for (String s : tempContent) {
                        if (s.contains("+")) {
                            s = s.replaceAll("\\+", "-");
                            fw.append(s);
                            fw.append("\n");
                        }
                    }
                }
            }
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "appendToOne");
        }

    }

    private void appendToOthers(ArrayList<File> fileList, File newFile) {
        ArrayList<String> newFileContent = file2stringList(newFile);
        for (File tempFile : fileList) {
            if (tempFile.isFile()) {
                String tempFileName = tempFile.getName();
                try {
                    FileWriter fw = new FileWriter(tempFile, true);
                    for (String tempString : newFileContent) {
                        tempString = tempString.replaceAll("\\+", "-");
                        fw.append(tempString);
                        fw.append("\n");
                    }
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "appendToOthers");
                }
            }
        }

    }

    // ∆ƒ¿œ »Æ¿Â¿⁄∏¶ æÚæÓø¿¥¬ ∏ﬁº“µÂ
    private String getFileType(String fileName) {
        int pos = fileName.lastIndexOf(".");
        String ext = fileName.substring(pos + 1);
        return ext;
    }

    // ∆˙¥ı ≥ª∫Œ ∆ƒ¿œ ∏ÆΩ∫∆Æ∏¶ æÚæÓø¿¥¬ ∏ﬁº“µÂ
    private ArrayList<File> getFileList(String dirPath) {

        File dirFile = new File(dirPath);
        File[] tmepFileList = dirFile.listFiles();
        ArrayList<File> fileList = new ArrayList<File>();

        for (File tempFile : tmepFileList) {
            if (tempFile.isFile()) {
                String tempPath = tempFile.getParent();
                String tempFileName = tempFile.getName();
                if (getFileType(tempFileName).equalsIgnoreCase( TRAINFILE_EXTENSION ) ) {
                    fileList.add(tempFile);
                }
                /*** Do something withd tempPath and temp FileName ^^; ***/
            }
        }
        return fileList;
    }

    private ArrayList<String> file2stringList(File f) {
        ArrayList<String> stringList = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(f));
            String s;

            while ((s = in.readLine()) != null) {
                stringList.add(s);
            }
            in.close();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "file2stringList");
        }

        return stringList;
    }

}
