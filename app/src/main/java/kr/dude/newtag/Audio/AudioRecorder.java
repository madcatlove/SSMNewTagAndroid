package kr.dude.newtag.Audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.dude.newtag.Constant;

/**
 * Created by madcat on 1/13/16.
 */
public class AudioRecorder {

    private static final String LOG_TAG = "AudioRecorder";

    private Context mContext;
    private boolean isRecording = false;
    private AudioRecord ar = null;
    private int minBufferSize = 0;
    private int mBufferSizeInBytes = 0;
    private int mAudioLen = 0; // 오디오 파일 길이( 임시파일 사이즈 )

    private final int WAVE_HEADER_SIZE = 44;
    private final int AUDIO_SAMPLE_RATE = 44100;

    private final int AUDIO_CHANNEL = AudioFormat.CHANNEL_IN_STEREO; // AudioFormat.CHANNEL_IN_STEREO
    private final int AUDIO_CHANNEL_TYPE = 2; // 1모노 2스테레오

    private final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final int AUDIO_BIT_PER_SAMPLE = 16;

    private String recentFilePath = null;


    /* 레코더 종료시 실행할 콜백 */
    private CompleteCallback mRecorderOnComplete = null;

    public interface CompleteCallback {
        public void executeTaskOnRecorderStopped(String filePath);
    }

    public AudioRecorder(Context context) {
        mContext = context;
    }

    public void setRecorderOnComplete(CompleteCallback callback) {
        mRecorderOnComplete = callback;
    }


    /**
     * 녹음 시작
     */
    public void startRecord() {

        minBufferSize = AudioRecord.getMinBufferSize(AUDIO_SAMPLE_RATE, AUDIO_CHANNEL, AUDIO_ENCODING);

        if (AudioRecord.ERROR_BAD_VALUE == minBufferSize) {
            throw new RuntimeException("Bad Audio Configuration value");
        }

        mBufferSizeInBytes = minBufferSize; // FOR FILE OPERATION (아마 *2해도될듯 )

        final Runnable record_thread = new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, " :: RECORD THREAD RUNNING !! ");

                ar = new AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        AUDIO_SAMPLE_RATE,
                        AUDIO_CHANNEL,
                        AUDIO_ENCODING,
                        mBufferSizeInBytes);


                isRecording = true;
                recentFilePath = null;
                ar.startRecording();
                writeAudioDataFile();

                releaseRecorder();

                Log.w(LOG_TAG, " :: RECORD THREAD STOPPED !! ");

                /* 콜백 실행 */
                if( mRecorderOnComplete != null && recentFilePath != null) {
                    Log.i(LOG_TAG, "Execute Callback (onRecorderStopped())");
                    mRecorderOnComplete.executeTaskOnRecorderStopped(recentFilePath);
                }


            }
        };


        // 레코딩 시작
        Thread t = new Thread(record_thread);
        t.start();


    }

    /**
     * 녹음 중단
     */
    public void stopRecord() {
        isRecording = false;
    }

    private void releaseRecorder() {
        ar.stop();
        ar.release();
    }

    /**
     * 녹음된 파일 쓰기
     */
    private void writeAudioDataFile() {

        String currentDateStr = (new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss")).format(new Date());

        final String FILE_DIR = Constant.getSdcardPath() + "/newTag/";
        final String FILE_NAME = "" + currentDateStr + ".wav";
        final String FILE_TEMP_NAME = "" + FILE_NAME + "_temp";

        byte[] data = new byte[mBufferSizeInBytes]; // 실제 레코더로부터 받는 데이터
        byte[] buffer = new byte[mBufferSizeInBytes]; // 임시 파일에서 받는 데이터

        try {
            makeFolder(FILE_DIR);

            //--------------------------------------------------------------------------------------
            // 임시파일 작성
            FileOutputStream tempFileOpen =
                    new FileOutputStream(new File( FILE_DIR + FILE_TEMP_NAME));

            // 녹음중일때만..
            int read = 0;
            while (isRecording) {
                read = ar.read(data, 0, mBufferSizeInBytes);

                // 에러가 없다면 파일 작성
                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    tempFileOpen.write(data);
                }
            }

            tempFileOpen.flush();
            mAudioLen = (int) tempFileOpen.getChannel().size();
            tempFileOpen.close();
            //--------------------------------------------------------------------------------------


            //--------------------------------------------------------------------------------------
            // 실제 WAVE 파일 작성 ( 헤더 + TEMP파일 읽기 )
            FileInputStream tempFIS = new FileInputStream(new File(FILE_DIR + FILE_TEMP_NAME));
            FileOutputStream  dataFOS = new FileOutputStream(new File(FILE_DIR + FILE_NAME));

            read = 0;

            // 헤더 작성
            dataFOS.write( getWaveHeader() );

            // 데이터 영역 작성
            while( (read = tempFIS.read(buffer)) != -1) {
                dataFOS.write(buffer);
            }

            tempFIS.close();
            dataFOS.flush();
            dataFOS.close();
            //--------------------------------------------------------------------------------------

            Log.i(LOG_TAG, "Complete file write wave file " + FILE_DIR + FILE_NAME);

            /* 파일 디렉토리 갱신 */
            recentFilePath = FILE_DIR + FILE_NAME;

        } catch (IOException e) {
            Log.e(LOG_TAG, "There is no file " + FILE_DIR + FILE_NAME);
            e.printStackTrace();
        }

    }

    /**
     * 웨이브 헤더 리턴
     */
    private byte[] getWaveHeader() {

        byte[] header = new byte[WAVE_HEADER_SIZE];
        int totalDataLen = mAudioLen + 40;

        long byteRate = AUDIO_BIT_PER_SAMPLE * AUDIO_SAMPLE_RATE * AUDIO_CHANNEL_TYPE / 8;
        header[0] = 'R';  // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f';  // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = (byte) 1;  // format = 1 (PCM방식)
        header[21] = 0;
        header[22] = AUDIO_CHANNEL_TYPE;
        header[23] = 0;
        header[24] = (byte) (AUDIO_SAMPLE_RATE & 0xff);
        header[25] = (byte) ((AUDIO_SAMPLE_RATE >> 8) & 0xff);
        header[26] = (byte) ((AUDIO_SAMPLE_RATE >> 16) & 0xff);
        header[27] = (byte) ((AUDIO_SAMPLE_RATE >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) AUDIO_BIT_PER_SAMPLE * AUDIO_CHANNEL_TYPE / 8;  // block align
        header[33] = 0;
        header[34] = AUDIO_BIT_PER_SAMPLE;  // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (mAudioLen & 0xff);
        header[41] = (byte) ((mAudioLen >> 8) & 0xff);
        header[42] = (byte) ((mAudioLen >> 16) & 0xff);
        header[43] = (byte) ((mAudioLen >> 24) & 0xff);
        return header;
    }


    private void makeFolder(String path) {
        File f = new File(path);

        if( !f.exists()) {
            f.mkdir();
        }

    }
}
