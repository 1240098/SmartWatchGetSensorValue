package com.example.akirakoumatsuoka.get_activity_for_android44;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity implements SensorEventListener{//,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = MainActivity.class.getName();;

    private static final int REQUEST_CODE_RESOLUTION = 1;

    private GoogleApiClient googleApiClient = null;


    //private final String TAG = MainActivity.class.getName();
    private TextView mTextView;

    private SensorManager mSensorManager;
    private GoogleApiClient mGoogleApiClient;
    private String mNode;
    private float x,y,z,gx,gy,gz,Heart=0.0f,Step,lx,ly,lz;

    int count = 0;
    // boolean set = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextView = (TextView) findViewById(R.id.text);
        //hTextView = (TextView) findViewById(R.id.heart);
        //  mTextView.setTextSize(30.0f);
        // mTextView = (TextView) findViewById(R.id.text);




        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)

                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.d(TAG, "onConnected");
                        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                            @Override
                            public void onResult(NodeApi.GetConnectedNodesResult nodes) {
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

    private LinearLayout.LayoutParams createParam(int w, int h){
        return new LinearLayout.LayoutParams(w, h);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        Sensor gyro=mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);//we can choose Fastest Normal
        Sensor hb=mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this,hb,SensorManager.SENSOR_DELAY_FASTEST);
Sensor step=mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
mSensorManager.registerListener(this,step,SensorManager.SENSOR_DELAY_FASTEST);

        Sensor linear =mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        mSensorManager.registerListener(this,linear,SensorManager.SENSOR_DELAY_FASTEST);

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
        Calendar cl = Calendar.getInstance();


        if(count>= 5) {
            count = 0;
            // Calendar cl = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
            String now_time = sdf.format(cl.getTime());

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];
                //mTextView.setText(String.format("X : %f\nY : %f\nZ : %f" , x, y, z));


            }
            if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                gx = event.values[0];
                gy = event.values[1];
                gz = event.values[2];
                //gyroTextView.setText(String.format("X : %f\nY : %f\nZ : %f" , gx, gy, gz));
            }

            // if(set==false)mTextView.setText("");
            if (event.sensor.getType() == Sensor.TYPE_HEART_RATE) {
                mTextView.setText(String.valueOf(event.values[0]));
                Heart = event.values[0];
                //  set = true;
                System.out.println("test");
            }

            if(event.sensor.getType()==Sensor.TYPE_STEP_COUNTER){

                Step=event.values[0];
            }
            if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
                lx=event.values[0];
                ly=event.values[1];
                lz=event.values[2];

            }

            String SEND_DATA = x + "," + y + "," + z+","+lx + "," + ly + "," +lz+","+gx + "," + gy + "," +gz+","+Heart+","+now_time+","+Step;
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



            mTextView.setText(String.format("ACCELEROMETER\nX : %f\nY : %f\nZ : %f\nGYROSCOPE\nX : %f\nY : %f\nZ : %f\nHeartBeat %d step %d" , x, y, z,gx,gy,gz,(int)Heart,(int)Step));
        }else count++;
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


}