package com.example.akirakoumatsuoka.get_activity_for_android44;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;



public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener{//,View.OnClickListener {
    private static final String TAG = MainActivity.class.getName();
    private GoogleApiClient mGoogleApiClient;
    TextView xTextView;
    TextView yTextView;
    TextView zTextView;
    TextView gxTextView;
    TextView gyTextView;
    TextView gzTextView;
    TextView hTextView;
    TextView COUNT;
    private TextView textView;
    EditText editText_name,editText_Activity,Time;
    public int button_flag = -1,flag=0,swich=0,filenumber=0,a=0;
    private String x,y,z,gx,gy,gz,h,time,lx,ly,lz;
    String now_time,Name,Activity,s,STEP="0";
    int count=0,date,second,i,file_count=0,Step=0,Prestep=0,fc;
    Calendar calendar;
    String[] names = new String[]{"x-value", "y-value", "z-value"};
    int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};
    LineChart mChart,mChart_gyro,mChart_hart;



    Timer timer,timer1;




    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };





    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //GoogleApiClientインスタンス生成
        setContentView(R.layout.activity_main);
        xTextView = (TextView)findViewById(R.id.xValue);
        yTextView = (TextView)findViewById(R.id.yValue);
        zTextView = (TextView)findViewById(R.id.zValue);
        gxTextView = (TextView)findViewById(R.id.gx);
        gyTextView = (TextView)findViewById(R.id.gy);
        gzTextView = (TextView)findViewById(R.id.gz);
        hTextView=(TextView)findViewById(R.id.textView2);

        COUNT=(TextView)findViewById(R.id.textView8);
        ActionBar ab = getSupportActionBar();
        //ab.hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        textView = findViewById(R.id.text_view);

        editText_name = (EditText)findViewById(R.id.edit_text);
        editText_Activity=(EditText)findViewById(R.id.editText);
        Time=(EditText)findViewById(R.id.editText2);
        COUNT.setText("0");

        Button start_btn = (Button) findViewById(R.id.button_save);
        Button stop_btn = (Button) findViewById(R.id.button_read);

        start_btn.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View v) {
                timer = new Timer(false);

                s = Time.getText().toString();
                i=Integer.parseInt(s);



                textView.setText("Start");
                button_flag = 1;

                if(i!=0) {
                    TimerTask task = new TimerTask() {

                        @Override
                        public void run() {
                            button_flag = 0;

                            timer.cancel();

                        }
                    };
                    timer.schedule(task, i*1000);

                }


                Name=editText_name.getText().toString();
                Activity=editText_Activity.getText().toString();

            }
        });

        stop_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("Stop");
                button_flag = 2;

            }
        });



//permission
/*        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }*/

        // idがswitchButtonのSwitchを取得
        Switch switchButton = (Switch) findViewById(R.id.switch1);
        // switchButtonのオンオフが切り替わった時の処理を設定
        switchButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener(){
                    public void onCheckedChanged(CompoundButton comButton, boolean isChecked){
                        // 表示する文字列をスイッチのオンオフで変える
                        String displayChar = "";
                        // オンなら
                        if(isChecked){
                            displayChar = "オンの状態です！";
                            swich=1;
                            file_count=0;
                        }
                        // オフなら
                        else{
                            swich=0;
                            displayChar = "オフの状態です。";
                            file_count=1;
                        }
                        Toast toast = Toast.makeText(MainActivity.this, displayChar, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );




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
        mChart = (LineChart) findViewById(R.id.lineChart);
        mChart.setDescription(null); // 表のタイトルを空にする
        mChart.setData(new LineData()); // 空のLineData型インスタンスを追加

        mChart_gyro=(LineChart)findViewById(R.id.lineChartGyro);
        mChart_gyro.setDescription(null);
        mChart_gyro.setData(new LineData());

        mChart_hart=(LineChart)findViewById(R.id.lineCharthart);
        mChart_hart.setDescription(null);
        mChart_hart.setData(new LineData());
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
        //xTextView.setText(messageEvent.getPath());
        String msg = messageEvent.getPath();
        String[] value = msg.split(",", 0);

        x = value[0];
        y = value[1];
        z = value[2];
        lx = value[3];
        ly = value[4];
        lz = value[5];
        gx = value[6];
        gy = value[7];
        gz = value[8];
        h = value[9];
        time = value[10];
        STEP = value[11];


        xTextView.setText(String.valueOf(value[0]));
        yTextView.setText(String.valueOf(value[1]));
        zTextView.setText(String.valueOf(value[2]));
        gxTextView.setText(String.valueOf(value[6]));
        gyTextView.setText(String.valueOf(value[7]));
        gzTextView.setText(String.valueOf(value[8]));
        hTextView.setText(String.valueOf(value[9]));

        if(swich==0){
           if (button_flag == 1) {
            /*xTextView.setText("x:"+String.valueOf(value[0]));
        yTextView.setText("y:"+String.valueOf(value[1]));
        zTextView.setText("z:"+String.valueOf(value[2]));
        gxTextView.setText("gx:"+String.valueOf(value[3]));
        gyTextView.setText("gy:"+String.valueOf(value[4]));
        gzTextView.setText("gz:"+String.valueOf(value[5]));
        hTextView.setText("HB:"+String.valueOf(value[6]));
*/
            Step = (int) Float.parseFloat(STEP);
            if (Step != Prestep) {
                //System.out.println("a");
                flag++;
                if (flag == 1) {
                   // System.out.println("b");
                    Prestep = Step;


                } else if (flag == 2) {
                    //System.out.println("c");
                    flag = 0;
                }

            }

            try {

                if (count == 0) {

                    Calendar cl = Calendar.getInstance();

                    calendar = cl;
                }

                SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmm");
                now_time = sdf.format(calendar.getTime());

                //now_time = String.valueOf(year) + "_" + String.valueOf(month) + "_" + String.valueOf(date) + "_" + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                // String Time = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                System.out.print("now:" + now_time);


                COUNT.setText(Integer.toString(file_count));
                count++;
                try {
                    //出力先を作成する


                    FileWriter fw = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"
                            + Activity + "_" + Name + "_" + s + "_" + String.format("%02d", file_count) + "_" + now_time + ".csv", true);
                    PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

                    String write_data = time + "," + x + "," + y + "," + z + "," + lx + "," + ly + "," + lz +"," + gx + "," + gy + "," + gz + "," + h + "," + String.format("%d", Step - Prestep)
                            + "\n";
                    System.out.print(write_data);
                    //内容を指定する
                    pw.print(write_data);

                    pw.close();






                   /* Uri uri =Uri.parse("file://" +Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+nowtime+".csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
*/


                } catch (IOException ex) {
                    //例外時処理
                    ex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (button_flag == 0) {
            //System.out.println(Name+" "+Activity);

            Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"
                    + Activity + "_" + Name + "_" + s + "_" + String.format("%02d", file_count) + "_" + now_time + ".csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
            sendBroadcast(new Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            count = 0;

            file_count++;
            COUNT.setText(Integer.toString(file_count));
            button_flag = 1;


            timer = new Timer(false);
            if (i != 0) {
                TimerTask task = new TimerTask() {

                    @Override
                    public void run() {
                        button_flag = 0;

                        timer.cancel();
                    }
                };
                timer.schedule(task, i * 1000);
            }

        } else if (button_flag == 2) {
            count = 0;
            if (i != 0) {
                File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"
                        + Activity + "_" + Name + "_" + s + "_" + String.format("%02d", file_count) + "_" + now_time + ".csv");
                file.delete();
                timer.cancel();
                button_flag = -1;
            } else {
                Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"
                        + Activity + "_" + Name + "_" + s + "_" + String.format("%02d", file_count) + "_" + now_time + ".csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            }


        }
    }

    //モード変更
    else if(swich==1){
            if (button_flag == 1) {
               if(a==0) {

                   COUNT.setText(Integer.toString(filenumber));
                   filenumber++;
               }
               a++;
            /*xTextView.setText("x:"+String.valueOf(value[0]));
        yTextView.setText("y:"+String.valueOf(value[1]));
        zTextView.setText("z:"+String.valueOf(value[2]));
        gxTextView.setText("gx:"+String.valueOf(value[3]));
        gyTextView.setText("gy:"+String.valueOf(value[4]));
        gzTextView.setText("gz:"+String.valueOf(value[5]));
        hTextView.setText("HB:"+String.valueOf(value[6]));
*/
                textView.setText("Start");
                Step = (int) Float.parseFloat(STEP);
                if (Step != Prestep) {
                    System.out.println("a");
                    flag++;
                    if (flag == 1) {
                        System.out.println("b");
                        Prestep = Step;


                    } else if (flag == 2) {
                        System.out.println("c");
                        flag = 0;
                    }

                }

                try {

                    if (count == 0) {

                        Calendar cl = Calendar.getInstance();

                        calendar = cl;
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
                    now_time = sdf.format(calendar.getTime());

                    //now_time = String.valueOf(year) + "_" + String.valueOf(month) + "_" + String.valueOf(date) + "_" + String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                    // String Time = String.valueOf(hour) + ":" + String.valueOf(minute) + ":" + String.valueOf(second);
                    System.out.print("now:" + now_time);



                    count++;
                    try {
                        //出力先を作成する


                        FileWriter fw = new FileWriter(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"+String.format("%02d", file_count)+"_"
                                + now_time+"_"+ Name+"_"+String.format("%02d", file_count)+ ".csv", true);
                        PrintWriter pw = new PrintWriter(new BufferedWriter(fw));

                        String write_data = time + "," + x + "," + y + "," + z + "," + lx + "," + ly + "," + lz +"," + gx + "," + gy + "," + gz + "," + h + "," + String.format("%d", Step - Prestep)
                                + "\n";
                        System.out.print(write_data);
                        //内容を指定する
                        pw.print(write_data);

                        pw.close();






                   /* Uri uri =Uri.parse("file://" +Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath()+"/"+nowtime+".csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
*/


                    } catch (IOException ex) {
                        //例外時処理
                        ex.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            } else if (button_flag == 0) {
                //System.out.println(Name+" "+Activity);
                a=0;
                Uri uri = Uri.parse("file://" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"+String.format("%02d", file_count)+"_"
                        + now_time+"_"+ Name+"_"+String.format("%02d", file_count)+ ".csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
                sendBroadcast(new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                count = 0;

                file_count++;
                fc=file_count;
                if(file_count==10)file_count=0;


                button_flag=3;
                timer1 = new Timer(false);
                if (i != 0) {
                    TimerTask task = new TimerTask() {

                        @Override
                        public void run() {
                            button_flag = 1;

                            timer1.cancel();
                        }
                    };
                    timer1.schedule(task, 2000);
                }




                timer = new Timer(false);
                if (i != 0) {
                    TimerTask task = new TimerTask() {

                        @Override
                        public void run() {
                            button_flag = 0;

                            timer.cancel();
                        }
                    };
                    timer.schedule(task, i * 1000+3000);
                }

            }
            else if (button_flag==3){
                textView.setText("Stop");
            }
            else if (button_flag == 2) {
                a=0;
                count = 0;
                if (i != 0) {
                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/"+String.format("%02d", fc)+"_"
                            + now_time+"_"+ Name+"_"+String.format("%02d", fc)+ ".csv");
                    file.delete();
                    timer.cancel();
                    button_flag = -1;
                } else {
                    Uri uri = Uri.parse(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/00_"
                            + now_time+"_"+ Name+"_00.csv");//RESULT_DIR/FILEは保存対象のパス
//mediascan:作ったファイルをPC上で見れるようにするため（自動化）。これないと再起動しないと見れない。
                    sendBroadcast(new Intent(
                            Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                }

            }
        }


        LineData data = mChart.getLineData();
        if (data != null) {
            for (int i = 0; i < 3; i++) {
                ILineDataSet set = data.getDataSetByIndex(i);
                if (set == null) {
                    set = createSet(names[i], colors[i]);
                    data.addDataSet(set);
                }

                data.addEntry(new Entry(set.getEntryCount(),  Float.parseFloat(value[i])), i);
                data.notifyDataChanged();
            }

            mChart.notifyDataSetChanged();
            mChart.setVisibleXRangeMaximum(500);
            mChart.moveViewToX(data.getEntryCount());
        }

        LineData data_gyro = mChart_gyro.getLineData();
        if (data_gyro != null) {
            for (int i = 0; i < 3; i++) {
                ILineDataSet set_gyro = data_gyro.getDataSetByIndex(i);
                if (set_gyro == null) {
                    set_gyro = createSet_gyro("gyro"+names[i], colors[i]);
                    data_gyro.addDataSet(set_gyro);
                }

                data_gyro.addEntry(new Entry(set_gyro.getEntryCount(), Float.parseFloat(value[i+6])), i);
                data_gyro.notifyDataChanged();
            }

            mChart_gyro.notifyDataSetChanged();
            mChart_gyro.setVisibleXRangeMaximum(500);
            mChart_gyro.moveViewToX(data.getEntryCount());
        }

        LineData data_hart = mChart_hart.getLineData();
        if (data_hart != null) {

            ILineDataSet set_hart = data_hart.getDataSetByIndex(0);
            if (set_hart == null) {
                set_hart = createSet_hart("hart", Color.RED);
                data_hart.addDataSet(set_hart);

            }
            data_hart.addEntry(new Entry(set_hart.getEntryCount(), Float.parseFloat(value[6])), 0);
            data_hart.notifyDataChanged();


            mChart_hart.notifyDataSetChanged();
            mChart_hart.setVisibleXRangeMaximum(500);
            //mChart_hart.moveViewToX(data.getEntryCount());
            mChart_gyro.moveViewToX(data.getEntryCount());
        }


    }

    private LineDataSet createSet(String label, int color) {
        LineDataSet set = new LineDataSet(null, label);
        set.setLineWidth(2.5f);
        set.setColor(color);
        set.setDrawCircles(false);
        set.setDrawValues(false);

        return set;
    }

    private LineDataSet createSet_gyro(String label, int color) {
        LineDataSet set_gyro = new LineDataSet(null, label);
        set_gyro.setLineWidth(2.5f);
        set_gyro.setColor(color);
        set_gyro.setDrawCircles(false);
        set_gyro.setDrawValues(false);

        return set_gyro;
    }

    private LineDataSet createSet_hart(String label, int color) {
        LineDataSet set_hart = new LineDataSet(null, label);
        set_hart.setLineWidth(2.5f);
        set_hart.setColor(color);
        set_hart.setDrawCircles(false);
        set_hart.setDrawValues(false);

        return set_hart;
    }

    // ファイルを保存
    public void saveFile(String file, String str) {

        // try-with-resources
        try (FileOutputStream fileOutputstream = openFileOutput(file,
                Context.MODE_PRIVATE);){

            fileOutputstream.write(str.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
