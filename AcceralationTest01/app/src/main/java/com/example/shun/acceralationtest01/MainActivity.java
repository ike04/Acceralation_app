package com.example.shun.acceralationtest01;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

public class MainActivity extends Activity {

    private SensorManager manager;
    private Sensor sensor;
    private SensorEventListener sample_listener;

    TextView tv_x;
    TextView tv_y;
    TextView tv_z;

    double x, y, z;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        setContentView( ll );
        tv_x = new TextView(this);
        tv_y = new TextView(this);
        tv_z = new TextView(this);
        ll.addView(tv_x);
        ll.addView(tv_y);
        ll.addView(tv_z);

        sample_listener = new SampleSensorEventListener();
    }

    public boolean isExternalStorageWritable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable(){
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            return true;
        }
        return false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        manager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor  = manager.getDefaultSensor( Sensor.TYPE_ACCELEROMETER );
        manager.registerListener(sample_listener, sensor, SensorManager.SENSOR_DELAY_NORMAL );
    }
    @Override
    protected void onPause(){
        super.onPause();
        manager.unregisterListener(sample_listener);
    }


    /*
    インナークラス SampleSensorEventListener
     */
    class SampleSensorEventListener implements SensorEventListener{

        @Override
        public void onSensorChanged(SensorEvent se) {
            if(se.sensor.getType() == Sensor.TYPE_ACCELEROMETER ){
                x = se.values[0];
                y = se.values[1];
                z = se.values[2];
                String str_x = "X軸の加速度:" + se.values[0];
                tv_x.setText( str_x );
                String str_y = "Y軸の加速度:" + se.values[1];
                tv_y.setText( str_y );
                String str_z = "Z軸の加速度:" + se.values[2];
                tv_z.setText( str_z );

                String fileName = "test.csv";
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
        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
        }

    }

}