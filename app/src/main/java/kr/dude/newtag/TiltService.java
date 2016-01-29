package kr.dude.newtag;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

import kr.dude.newtag.TiltManager.PitchRoll;
import kr.dude.newtag.TiltManager.PitchRollTracker;
import kr.dude.newtag.TiltManager.WifiScanResultReceiver;

/**
 * 가속도센서, 지자계 센서를 사용하여 Tilt 상태 확인
 */
public class TiltService extends Service {

    private static final String LOG_TAG = "TiltService";
    private SensorManager sensorManager;
    private Sensor accSensor;
    private Sensor magSensor;
    private final SensorEventListener mSensorEventListener;

    private float[] accValues = new float[3];
    private float[] magValues = new float[3];
    private float[] rotationMatrix = new float[9];

    private final PitchRollTracker pitchRollTrackerService = PitchRollTracker.getInstance();
    private BroadcastReceiver mWifiScanResultReceiver;

    public TiltService() {
        Log.e(LOG_TAG, "TiltService constructor");
        mSensorEventListener = new SensorEventReceiver();
        mWifiScanResultReceiver = new WifiScanResultReceiver();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate()");
        super.onCreate();

        // 센서 정보 가져옴
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if( accSensor != null) {
            Log.i(LOG_TAG, " REGISTER LISTENER ACC SENSOR ");
            sensorManager.registerListener(mSensorEventListener, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }

        if( magSensor != null) {
            Log.i(LOG_TAG, " REGISTER LISTENER MAG SENSOR ");
            sensorManager.registerListener(mSensorEventListener, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }


        // wifi broadcast receiver
        IntentFilter wifiIntentFilter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mWifiScanResultReceiver, wifiIntentFilter);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {



        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestory()");
        super.onDestroy();

        // 센서 이벤트 리스너 해제
        sensorManager.unregisterListener(mSensorEventListener);
        unregisterReceiver(mWifiScanResultReceiver);
    }


    private class SensorEventReceiver implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            /* 센서값 복제 */
            if( sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
                accValues = sensorEvent.values.clone();
            }

            if( sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magValues = sensorEvent.values.clone();
            }

            if( accValues != null && magValues != null) {
                boolean result = SensorManager.getRotationMatrix(rotationMatrix, null, accValues, magValues);

                if( result ) {
                    float[] orientationData = new float[3];
                    SensorManager.getOrientation(rotationMatrix, orientationData);

                    // [0] => Radian값 Degree
                    float rDegree = (float) Math.toDegrees( orientationData[0] );
                    if( rDegree < 0)  rDegree += 360;

                    // [1] => 경사도 (pitch)(x)
                    int rPitch = (int) Math.toDegrees( orientationData[1] );

                    // [2] => 좌우회전값 (roll)(y)
                    int rRoll = (int) Math.toDegrees( orientationData[2] );





                    // 데이터 전송
                    PitchRoll pitchRoll = new PitchRoll(rPitch, rRoll, System.currentTimeMillis());
                    pitchRollTrackerService.addItem(getApplicationContext(), pitchRoll);
                }
            }


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }
}
