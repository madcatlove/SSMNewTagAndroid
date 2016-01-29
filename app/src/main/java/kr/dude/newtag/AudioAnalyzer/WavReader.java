package kr.dude.newtag.AudioAnalyzer;

/**
 * Created by madcat on 1/28/16.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class WavReader {
    private static final int WAVE_HEADER_SIZE = 44;
    public static final int SAMPLE_RATE = 44100;
    private static final int BYTES_PER_SAMPLE = 2; // 16-bit audio
    private static final int BITS_PER_SAMPLE = 16; // 16-bit audio
    private static final double MAX_16_BIT = Short.MAX_VALUE; // 32,767
    private static final int SAMPLE_BUFFER_SIZE = 4096;


    public double[] read(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = null;

        fis = new FileInputStream(file);


        byte[] header = new byte[WAVE_HEADER_SIZE];
        fis.read(header, 0, WAVE_HEADER_SIZE);

        int fileSize = 0;

        int h40 = header[WAVE_HEADER_SIZE - 4] & 0xff;
        int h41 = header[WAVE_HEADER_SIZE - 3] & 0xff;
        int h42 = header[WAVE_HEADER_SIZE - 2] & 0xff;
        int h43 = header[WAVE_HEADER_SIZE - 1] & 0xff;

        fileSize = fileSize | h40;
        fileSize = fileSize | (h41 << 8);
        fileSize = fileSize | (h42 << 16);
        fileSize = fileSize | (h43 << 24);

        byte[] dataChunks = new byte[fileSize];

        fis.read(dataChunks, 0, fileSize);

        fis.close();

        int N = dataChunks.length;
        double[] d = new double[N / 2];
        for (int i = 0; i < N / 2; i++) {
            d[i] = ((short) (((dataChunks[2 * i + 1] & 0xFF) << 8) + (dataChunks[2 * i] & 0xFF)))
                    / ((double) MAX_16_BIT);
        }

        return d;
    }

}

