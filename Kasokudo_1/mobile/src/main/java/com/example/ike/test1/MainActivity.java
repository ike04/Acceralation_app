package com.example.ike.test1;


import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{
    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    TextView xTextView;
    TextView yTextView;
    TextView zTextView;
    double x,y,z;
    private String time = getDateString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        xTextView = (TextView)findViewById(R.id.xValue);
        yTextView = (TextView)findViewById(R.id.yValue);
        zTextView = (TextView)findViewById(R.id.zValue);
        ActionBar ab = getActionBar();
        //ab.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed:" + connectionResult.toString());
                    }
                })
                .addApi(Wearable.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mGoogleApiClient && mGoogleApiClient.isConnected()) {
            Wearable.MessageApi.removeListener(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        Wearable.MessageApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        xTextView.setText(messageEvent.getPath());
        String msg = messageEvent.getPath();
        String[] value = msg.split(",", 0);

        x = Double.valueOf(value[0]);
        y = Double.valueOf(value[1]);
        z = Double.valueOf(value[2]);

        xTextView.setText(String.valueOf(value[0]));
        yTextView.setText(String.valueOf(value[1]));
        zTextView.setText(String.valueOf(value[2]));

        writeCSVFile(x, y, z);

        /*
        x =  Integer.parseInt(value[0]);
        y =  Integer.parseInt(value[1]);
        z =  Integer.parseInt(value[2]);
        */


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

    /* 内部ストレージへのファイル書き込み用関数 */
    public void writeCSVFile(double x, double y, double z){
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