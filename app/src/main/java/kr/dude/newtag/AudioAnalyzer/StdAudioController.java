package kr.dude.newtag.AudioAnalyzer;

import java.io.IOException;
import java.util.ArrayList;

import kr.dude.newtag.AudioAnalyzer.FFTCalculator;
import kr.dude.newtag.AudioAnalyzer.WavReader;

/**
 * @author JeongTaek
 * @brief StdAudioø°º≠ ¿–æÓµÈ¿Œ .wav ∆ƒ¿œ¿ª FFT ∞°¥…«— πËø≠ «¸≈¬∑Œ ∫Ø»Ø«œ¥¬ ≈¨∑°Ω∫
 * @see StdAudio
 */
public class StdAudioController {

    private static final int WINDOW_SIZE = 512;
    private static final int SOUND_LENGTH = 100;
    private static final int FEATURE_LENGTH = 100;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;

    private static final int SAMPLING_RATE = 44100;
    private static final int GAP = 4000;

    private static final int FREQ_16 = 0; // 2∞≥
    private static final int FREQ_17 = 1; // 4∞≥
    private static final int FREQ_18 = 2; // 4∞≥
    private static final int FREQ_19 = 3; // 4∞≥
    private static final int FREQ_20 = 4; // 4∞≥

    private static final int MONO = 1;
    private static final int STEREO = 2;

    private String filePath = "";

    // WINDOW_SIZE(1024)∏¶ ¿ß«— FFT ∞¥√º ª˝º∫
    FFTCalculator mFftCalculator = new FFTCalculator(WINDOW_SIZE);

    int[] arrFreq = {16000, 17000, 18000, 19000, 20000};
    int[] arrTargetFreqSamplePos;

    public StdAudioController(String filePath) {
        this.filePath = filePath;
        arrTargetFreqSamplePos = new int[arrFreq.length];
        // ≈∏∞Ÿ¿Ã µ«¥¬ ¡÷∆ƒºˆ¿« ª˘«√ ¿ßƒ°∏¶ ¿˙¿Â«‘
        for (int arrFreqIdx = 0; arrFreqIdx < arrFreq.length; arrFreqIdx++) {
            arrTargetFreqSamplePos[arrFreqIdx] = (arrFreq[arrFreqIdx] * (WINDOW_SIZE / 2)) / (SAMPLING_RATE / 2);
        }
    }

    /**
     * @return
     * @throws IOException
     * @brief svm ««√ƒ∏¶ ªÃæ∆≥Ω¥Ÿ.
     */
    public String getSvmFeature(int channel, String label) throws IOException {
        WavReader mWavReader = new WavReader();
        // input wav ∆ƒ¿œ ≥°±Ó¡ˆ ¿–æÓµÈ¿”
        double[] mixedData = mWavReader.read(this.filePath);
        double[] leftData = null;
        double[] rightData = null;

        // Ω∫≈◊∑πø¿¿œ ∞ÊøÏ
        if (channel == MONO) {
            // input wav ∆ƒ¿œ ¡¬√¯ º“∏Æµ•¿Ã≈Õ
            leftData = mixedData;
            rightData = mixedData;

        } else if (channel == STEREO) {
            leftData = getLeftSound(mixedData);
            rightData = getRightSound(mixedData);
        } else {
            System.out.println("channel ø¿∑˘");
        }

        double[] filtered = filter(leftData);

        // √÷√  16kHzø° ¥Î«— ΩÃ≈©∏¶ æÚæÓø»
        int syncIdx = getSync(leftData);

        System.out.println("√÷√  16kHz ΩÃ≈© : " + syncIdx);

        // 16kHz 2∞≥, 17kHz 4∞≥, 18kHz 4∞≥, 19kHz 4∞≥, 20kHz 4∞≥
        int[] maxIdxs = new int[2 + 4 + 4 + 4 + 4];

        // 17kHz, 18kHz, 19kHz, 20kHzø° ¥Î«— wav««√ƒµÈ¿ª ¿œ¥‹ ∏∑ ¥„æ∆µ— ∏ÆΩ∫∆Æ
        ArrayList<double[]> tmpWavFeatures = new ArrayList<double[]>();

        // ¥‹º¯ pilot 2∞≥(16kHz 2∞≥)∏¶ ¡¶ø‹«— ≥™∏”¡ˆ 17, 18, 19, 20µÈ∑Œ∫Œ≈Õ ««√ƒ∏¶ ªÃæ∆≥Ω¥Ÿ.
        for (int i = 2; i < maxIdxs.length; i++) {
            // sytncIdx+ 0, 4100, 8200, 12300, ... ø° ¥Î«ÿº≠ ∞¢∞¢ maxIdx∏¶ ±∏«‘
            int maxIdx = getMaxIdx(filtered, syncIdx, syncIdx + (GAP + SOUND_LENGTH) * i);
            // ±∏«ÿ¡¯ maxIdx ¿Ã»ƒ 100∞≥∏¶ √Î«‘
            double[] wavFeature = sliceArr(filtered, maxIdx + 1, maxIdx + 1 + FEATURE_LENGTH);
            // feature∏¶ features∑Œ ª¿‘
            tmpWavFeatures.add(wavFeature);
        }

        // tmpWavFeaturesµÈ¿ª FFT«œø© ¥„¿ª ∏ÆΩ∫∆Æ
        ArrayList<double[]> tmpFftFeatures = new ArrayList<double[]>();

        for (int i = 0; i < tmpWavFeatures.size(); i++) {
            double[] tmpWavFeature = tmpWavFeatures.get(i);
            double[] real = new double[WINDOW_SIZE];
            double[] imag = new double[WINDOW_SIZE];
            for (int j = 0; j < tmpWavFeature.length; j++) {
                real[j] = tmpWavFeature[j];
            }
            // FFT
            mFftCalculator.fft(real, imag);

            // FFT ¿˝π›∏∏ ªı∑ŒøÓ πËø≠ø° ø≈∞‹ ¥„¿Ω
            double[] tmpFftFeature = new double[WINDOW_SIZE / 2];

            // FFT ∞·∞˙ æÁºˆ√≥∏Æ«ÿæﬂ«‘.
            for (int j = 0; j < tmpFftFeature.length; j++) {
                tmpFftFeature[j] = Math.abs(real[j]);
            }
            // fftTmpFeaturesø° √ﬂ∞°.
            tmpFftFeatures.add(tmpFftFeature);
        }

        // 16∞≥ ««≈©¿Ã»ƒ 100∞≥ø° ¥Î«— FFT ºˆ«‡¿ª ∏∂π´∏Æ «— ¥Ÿ¿Ωø£, 4∞≥æø π≠æÓ ∆Ú±’¿ª ≥ª¡‡æﬂ «—¥Ÿ.
        // «ˆ¿Á fftTmpFeaturesø°¥¬ 16∞≥¿« fft∞·∞˙µÈ¿Ã µÈæÓ¿÷¥Ÿ
        // fftFeaturesø°¥¬ 4∞≥¿« ∞·∞˙∞° µÈæÓ¿÷¿ª øπ¡§¿Ã¥Ÿ.
        ArrayList<double[]> fftFeatures = new ArrayList<double[]>();

        for (int i = 0; i < 4; i++) {
            double[] fftFeature = new double[WINDOW_SIZE / 2];
            for (int j = 0; j < WINDOW_SIZE / 2; j++) {
                fftFeature[j] = tmpFftFeatures.get(i * 4 + 0)[j] + tmpFftFeatures.get(i * 4 + 1)[j]
                        + tmpFftFeatures.get(i * 4 + 2)[j] + tmpFftFeatures.get(i * 4 + 3)[j];
            }
            // fftFeaturesø° ffeFeature∏¶ ¥„¥¬¥Ÿ.
            fftFeatures.add(fftFeature);
        }

        String svmFeature = "";
        int prefix = 1;
        for (int i = 0; i < fftFeatures.size(); i++) {
            double[] fftFeature = fftFeatures.get(i);
            // 22050Hz∞° 256∞≥¿« ¡°ø° ¥Î¿¿µ«π«∑Œ 1∞≥¿« ¡°ø° æ‡ 86Hz∞° «“¥Áµ»¥Ÿ. -6¿ª «ÿ¡÷¥¬ ¿Ã¿Ø¥¬ 17kHz -
            // 86*6Hz∫Œ≈Õ svm∫§≈Õ∑Œ ª¿‘«œ±‚ ¿ß«‘¿Ã¥Ÿ.
            for (int j = arrTargetFreqSamplePos[FREQ_17] - 6; j < WINDOW_SIZE / 2; j++) {
                svmFeature = svmFeature + Integer.toString(prefix) + ":" + Double.toString(fftFeature[j]) + " ";
                prefix++;
            }
        }
        svmFeature = label + " " + svmFeature;

        return svmFeature;

    }

    public double[][] getStereoArray(double[] mixedData) {

        double[][] stereoData;

        stereoData = new double[2][mixedData.length / 2];
        int posLeft = 0, posRight = 0;
        for (int i = 0; i < mixedData.length; i++) {
            if (i % 2 == 0)
                stereoData[LEFT][posLeft++] = mixedData[i];
            else
                stereoData[RIGHT][posRight++] = mixedData[i];
        }
        return stereoData;
    }

    public double[] getLeftSound(double[] mixedData) {
        double[] result = new double[mixedData.length / 2];
        int pos = 0;
        for (int i = 0; i < mixedData.length; i = i + 2) {
            result[pos++] = mixedData[i];
        }
        return result;
    }

    public double[] getRightSound(double[] mixedData) {

        double[] result = new double[mixedData.length / 2];
        int pos = 0;
        for (int i = 1; i < mixedData.length; i = i + 2) {
            result[pos++] = mixedData[i];
        }
        return result;
    }

    /**
     * @param input : √—«’¿ª ±∏«“ double πËø≠
     * @param start : start ¿ÃªÛ
     * @param end   : end πÃ∏∏
     * @return √—«’
     */
    private double getSum(double[] real, double[] imag, int start, int end) {
        double result = 0;
        for (int i = start; i < end; i++) {
            result = result + Math.sqrt(real[i] * real[i] + imag[i] * imag[i]);
        }
        return result;
    }

    /**
     * @param input : ΩÃ≈©∏¶ ¿‚æ∆≥æ ø¯∫ª πËø≠
     * @return
     */
    private int getSync(double[] input) {

        // fft ∞·∞˙ø°º≠ ∆Ø¡§ ¡÷∆ƒºˆ ¥Îø™¥Îø° «ÿ¥Á«œ¥¬ valueµÈ¿« «’¿ª ¿˙¿Â«“ πËø≠
        double fftResults[][] = new double[5][input.length];

        for (int inputIdx = 0; inputIdx < input.length; inputIdx++) {

            // ∞¢∞¢ ª˘«√∏∂¥Ÿ¿« window º¯»∏
            int windowIdx = 0;
            double windowSum = 0;

            double[] real = new double[WINDOW_SIZE];
            double[] imag = new double[WINDOW_SIZE];

            for (int i = inputIdx; i < inputIdx + SOUND_LENGTH; i++) {
                // window∞° input π¸¿ß∏¶ π˛æÓ≥Ø ∞ÊøÏ
                if (i >= input.length)
                    break;

                real[windowIdx] = input[i];
                windowIdx++;
            }
            // «— windowø° ¥Î«œø© FFT
            mFftCalculator.fft(real, imag);

            for (int j = 0; j < 5; j++) {
                windowSum = getSum(real, imag, arrTargetFreqSamplePos[j] - 1, arrTargetFreqSamplePos[j] + 1 + 1);
                fftResults[j][inputIdx] = windowSum;
            }

        }
        int syncIdx = 0;
        double maxValue = 0;
        // fftResult ∫–ºÆ
        for (int inputIdx = 0; inputIdx < input.length; inputIdx++) {
            double tmpValue = 0;

            for (int j = 0; j < 18; j++) {

                if (j < 2 && (inputIdx + (GAP + SOUND_LENGTH) * j) < input.length) {

                    tmpValue = tmpValue + fftResults[FREQ_16][inputIdx + (GAP + SOUND_LENGTH) * j];

                } else if (j < 2 + 4 && (inputIdx + (GAP + SOUND_LENGTH) * j) < input.length) {

                    tmpValue = tmpValue + fftResults[FREQ_17][inputIdx + (GAP + SOUND_LENGTH) * j];

                } else if (j < 2 + 4 + 4 && (inputIdx + (GAP + SOUND_LENGTH) * j) < input.length) {

                    tmpValue = tmpValue + fftResults[FREQ_18][inputIdx + (GAP + SOUND_LENGTH) * j];

                } else if (j < 2 + 4 + 4 + 4 && (inputIdx + (GAP + SOUND_LENGTH) * j) < input.length) {

                    tmpValue = tmpValue + fftResults[FREQ_19][inputIdx + (GAP + SOUND_LENGTH) * j];

                } else if (j < 2 + 4 + 4 + 4 + 4 && (inputIdx + (GAP + SOUND_LENGTH) * j) < input.length) {

                    tmpValue = tmpValue + fftResults[FREQ_20][inputIdx + (GAP + SOUND_LENGTH) * j];

                } else {
                    break;
                }

            }
            if (tmpValue > maxValue) {
                maxValue = tmpValue;
                syncIdx = inputIdx;
            }
        }

        return syncIdx;

    }

    // sync ¿Ã»ƒ º“∏Æ √÷¥Î∞™¿ª ≥™≈∏≥ª¥¬ ¡°¿ª ¿‚æ∆≥Ω¥Ÿ.
    public int getMaxIdx(double[] input, int startIdx, int endIdx) {
        int maxIdx = 0;
        double maxValue = 0;
        for (int i = startIdx; i < endIdx; i++) {
            if (input[i] >= maxValue) {
                maxIdx = i;
                maxValue = input[i];
            }
        }

        return maxIdx;
    }

    // ¿‘∑¬ πËø≠¿ª start ¿ÃªÛ, end πÃ∏∏¿∏∑Œ ¿ﬂ∂Û≥Ω¥Ÿ.
    public double[] sliceArr(double[] input, int start, int end) {
        if (start >= end) {
            System.out.println("Error : sliceArr, start>=end ø°∑Ø¿‘¥œ¥Ÿ.");
            return null;
        }
        double[] result = new double[end - start];
        int tmpIdx = 0;
        for (int i = start; i < end; i++) {
            result[tmpIdx] = input[i];
            tmpIdx++;
        }
        return result;
    }

    // πÍµÂ∆–Ω∫ « ≈Õ∏¶ ∞≈ƒ£ ∞·∞˙∏¶ ∏Æ≈œ«ÿ¡÷¥¬ ∏ﬁº“µÂ
    public double[] filter(double[] input) {

        double arrBandPassFilter[] = {-0.0014, 0.0023, -0.0024, 0.0007, 0.0025, -0.006, 0.0075, -0.0059, 0.0019,
                0.0023, -0.0044, 0.0036, -0.0014, -0.0001, -0.0006, 0.0027, -0.0043, 0.0039, -0.0019, -0.0001, 0.0002,
                0.0014, -0.003, 0.0028, -0.0008, -0.0014, 0.0019, -0.0004, -0.0014, 0.0015, 0.0005, -0.003, 0.0039,
                -0.0024, 0.0002, 0.0004, 0.0014, -0.0042, 0.0054, -0.0038, 0.0009, 0.0004, 0.0011, -0.0041, 0.0055,
                -0.0037, 0.0001, 0.0021, -0.0011, -0.0021, 0.004, -0.0022, -0.0021, 0.0053, -0.0047, 0.0011, 0.0015,
                -0.0002, -0.0046, 0.0087, -0.0084, 0.0041, -0.0002, 0.0006, -0.0055, 0.0103, -0.0102, 0.005, 0.0008,
                -0.0017, -0.003, 0.0086, -0.0089, 0.0027, 0.0051, -0.0078, 0.0032, 0.0036, -0.0049, -0.002, 0.012,
                -0.0164, 0.0116, -0.0026, -0.0009, -0.006, 0.0182, -0.0246, 0.019, -0.0062, -0.0012, -0.0047, 0.0194,
                -0.0285, 0.0215, -0.0026, -0.0119, 0.0082, 0.0106, -0.0251, 0.0165, 0.014, -0.0433, 0.0454, -0.017,
                -0.0125, 0.0019, 0.0665, -0.164, 0.2272, -0.201, 0.0804, 0.0804, -0.201, 0.2272, -0.164, 0.0665, 0.0019,
                -0.0125, -0.017, 0.0454, -0.0433, 0.014, 0.0165, -0.0251, 0.0106, 0.0082, -0.0119, -0.0026, 0.0215,
                -0.0285, 0.0194, -0.0047, -0.0012, -0.0062, 0.019, -0.0246, 0.0182, -0.006, -0.0009, -0.0026, 0.0116,
                -0.0164, 0.012, -0.002, -0.0049, 0.0036, 0.0032, -0.0078, 0.0051, 0.0027, -0.0089, 0.0086, -0.003,
                -0.0017, 0.0008, 0.005, -0.0102, 0.0103, -0.0055, 0.0006, -0.0002, 0.0041, -0.0084, 0.0087, -0.0046,
                -0.0002, 0.0015, 0.0011, -0.0047, 0.0053, -0.0021, -0.0022, 0.004, -0.0021, -0.0011, 0.0021, 0.0001,
                -0.0037, 0.0055, -0.0041, 0.0011, 0.0004, 0.0009, -0.0038, 0.0054, -0.0042, 0.0014, 0.0004, 0.0002,
                -0.0024, 0.0039, -0.003, 0.0005, 0.0015, -0.0014, -0.0004, 0.0019, -0.0014, -0.0008, 0.0028, -0.003,
                0.0014, 0.0002, -0.0001, -0.0019, 0.0039, -0.0043, 0.0027, -0.0006, -0.0001, -0.0014, 0.0036, -0.0044,
                0.0023, 0.0019, -0.0059, 0.0075, -0.006, 0.0025, 0.0007, -0.0024, 0.0023, -0.0014};
        double[] result = new double[input.length + arrBandPassFilter.length - 1];

        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < arrBandPassFilter.length; j++) {
                result[i + j] = input[i] * arrBandPassFilter[j] + result[i + j];
            }
        }

        return result;

    }

}
