package com.example.ike.test1;


import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import java.util.Random;




public class MainActivity extends Activity implements SensorEventListener {
    private final String TAG = MainActivity.class.getName();
    private final float GAIN = 0.9f;

    private TextView mTextView;
    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private float x,y,z;
    Random rnd = new Random();
    int count = 0;
    //final DateFormat df = new SimpleDateFormat("HH:mm:ssSSS");
    private Date date;
    private String time = getDateString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.text);
        mTextView.setTextSize(30.0f);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");

//                        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                                //Nodeは１個に限定
                                if (nodes.getNodes().size() > 0) {
                                    mNode = nodes.getNodes().get(0).getId();
                                }
                            }
                        });
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.d(TAG, "onConnectionSuspended");

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed : " + connectionResult.toString());
                    }
                })
                .build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(count>= 10) {
            count = 0;
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                //x = (x * GAIN + event.values[0] * (1 - GAIN));
                //y = (y * GAIN + event.values[1] * (1 - GAIN));
                //z = (z * GAIN + event.values[2] * (1 - GAIN));
                /*
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                */
                x = rnd.nextFloat() * 200 + 1;
                y = rnd.nextFloat() * 200 + 1;
                z = rnd.nextFloat() * 200 + 1;

                if (mTextView != null)
                    mTextView.setText(String.format("X : %f\nY : %f\nZ : %f" , x, y, z));

                writeCSVFile(x, y, z);

                //転送セット
                String SEND_DATA = x + "," + y + "," + z;
                if (mNode != null) {
                    Wearable.MessageApi.sendMessage(mGoogleApiClient, mNode, SEND_DATA, null).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult result) {
                            if (!result.getStatus().isSuccess()) {
                                Log.d(TAG, "ERROR : failed to send Message" + result.getStatus());
                            }
                        }
                    });
                }
            }
        }else count++;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /* 内部ストレージへのファイル書き込み用関数 */
    public void writeCSVFile(float x, float y, float z){
        String fileName = "test_" + time + ".csv";
        String str = String.format("X = %f, Y = %f, Z = %f, ", x, y, z);
        String path = Environment.getExternalStorageDirectory().getPath();
        String file_path = path + "/" + fileName;
        System.out.println("path = " + file_path);

        File file = new File(file_path);
        file.getParentFile().mkdir();

        FileOutputStream fos;
        try{
            fos = new FileOutputStream(file, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
            BufferedWriter bw = new BufferedWriter(osw);
            bw.write(str);
            bw.flush();
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /* ただの日付取得関数 */
    public static String getDateString(){
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}


