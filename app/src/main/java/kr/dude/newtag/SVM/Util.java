package kr.dude.newtag.SVM;

import android.os.Environment;

/**
 * Created by madcat on 1/27/16.
 */
public class Util {
    public static double atof(String s) {
        double d = Double.valueOf(s).doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d))
        {
           throw new RuntimeException("ATOF :: CANNOT CONVERT");
        }
        return(d);
    }

    public static int atoi(String s)
    {
        return Integer.parseInt(s);
    }

    public static String getSVMDir() {
        return Environment.getExternalStorageDirectory() + "/newTag/";
    }
}
