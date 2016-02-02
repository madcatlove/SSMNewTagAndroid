package kr.dude.newtag.AudioAnalyzer;

import java.io.IOException;

public class FeatureExtractor {
    private static int MONO = 1;
    private static int STEREO = 2;

    public String getSvmFeature(String filePath) throws IOException {
        return getSvmFeature(filePath, "+1");
    }

    public String getSvmFeature(String filePath, String label) throws IOException {
        StdAudioController mStdAudioController = new StdAudioController(filePath);
        String svmFeature = mStdAudioController.getSvmFeature(STEREO, label);
        return svmFeature;
    }

}
