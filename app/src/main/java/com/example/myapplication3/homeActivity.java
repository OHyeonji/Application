package com.example.myapplication3;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class homeActivity extends AppCompatActivity {
    private Handler mHandler;
    Socket socket;
    private String ip="192.168.219.106"; //서버 ip 192.168.40.203
    private int port = 9999; //포트 번호 맞추기
    EditText cap_input;
    TextView water_value;
    String value;
    TextView water_lv;
    private SharedPreferences sp;
    int int_water;
    ProgressBar bar;
    int total =0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //
        water_value = (TextView) findViewById(R.id.today_water);
        value = water_value.getText().toString();
        bar = (ProgressBar) findViewById(R.id.Bar);
        water_lv = (TextView) findViewById(R.id.water_lv_msg);

        //
        cap_input = (EditText) findViewById(R.id.capacityinput);
        Button btn = (Button) findViewById(R.id.Inputenter);
        Button r_button = (Button) findViewById(R.id.Reset_button);

        //값 저장
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        if ((sp != null) && (sp.contains("value"))) {
            value = sp.getString("value", "");
            total = sp.getInt("total",0);
            water_value.setText(value);
            color(water_value);
            setBar(total);
        }
        // 통신
        mHandler = new Handler();

        btn.setOnClickListener(v -> {
            if (cap_input.getText().toString() != null || !cap_input.getText().toString().equals("")) {
                ConnectThread th = new ConnectThread();
                th.setDaemon(true);
                th.start();
                Toast.makeText(homeActivity.this, "SENT", Toast.LENGTH_SHORT).show();
                String str = cap_input.getText().toString().trim();
                int num = Integer.parseInt(str);
                total = total + num;
                int_water = Integer.parseInt(value);
                int sum = int_water + num;
                water_value.setText("" + sum);
                save(water_value);
                color(water_value);
                setBar(total);
            }
        });
        //초기화
        r_button.setOnClickListener(v -> {
            ImageView one = (ImageView) findViewById(R.id.imageView);
            ImageView two = (ImageView) findViewById(R.id.imageView2);
            ImageView three = (ImageView) findViewById(R.id.imageView3);
            ImageView four = (ImageView) findViewById(R.id.imageView4);
            ImageView five = (ImageView) findViewById(R.id.imageView5);

            water_value.setText("0");
            save(water_value);
            one.setColorFilter(Color.parseColor("#A9A9A9"));
            two.setColorFilter(Color.parseColor("#A9A9A9"));
            three.setColorFilter(Color.parseColor("#A9A9A9"));
            four.setColorFilter(Color.parseColor("#A9A9A9"));
            five.setColorFilter(Color.parseColor("#A9A9A9"));
        });
    }

    // 프로그래스바
    private void setBar(int total) {
        int filter_value = (total/9000);
        if (filter_value<=60){
            bar.setProgress(filter_value);
            bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(000,153,000)));
        }
        else if (filter_value<=90){
            bar.setProgress(filter_value);
            bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(255,153,000)));
        }
        else{
            bar.setProgress(filter_value);
            bar.setProgressTintList(ColorStateList.valueOf(Color.rgb(255,51,000)));
            Toast.makeText(homeActivity.this,"Change the Filter",Toast.LENGTH_SHORT).show();
        }
    }
    // 이미지 색 채우기
    public void color(View v){
        ImageView one = (ImageView) findViewById(R.id.imageView);
        ImageView two = (ImageView) findViewById(R.id.imageView2);
        ImageView three = (ImageView) findViewById(R.id.imageView3);
        ImageView four = (ImageView) findViewById(R.id.imageView4);
        ImageView five = (ImageView) findViewById(R.id.imageView5);

        if (Integer.parseInt(value)>=2000) {
            one.setColorFilter(Color.parseColor("#FFADC8E6"));
            two.setColorFilter(Color.parseColor("#FFADC8E6"));
            three.setColorFilter(Color.parseColor("#FFADC8E6"));
            four.setColorFilter(Color.parseColor("#FFADC8E6"));
            five.setColorFilter(Color.parseColor("#FFADC8E6"));
        }
        else if (Integer.parseInt(value) >= 1600){
            one.setColorFilter(Color.parseColor("#FFADC8E6"));
            two.setColorFilter(Color.parseColor("#FFADC8E6"));
            three.setColorFilter(Color.parseColor("#FFADC8E6"));
            four.setColorFilter(Color.parseColor("#FFADC8E6"));
        }
        else if (Integer.parseInt(value) >= 1200){
            one.setColorFilter(Color.parseColor("#FFADC8E6"));
            two.setColorFilter(Color.parseColor("#FFADC8E6"));
            three.setColorFilter(Color.parseColor("#FFADC8E6"));
        }
        else if (Integer.parseInt(value) >= 800) {
            one.setColorFilter(Color.parseColor("#FFADC8E6"));
            two.setColorFilter(Color.parseColor("#FFADC8E6"));
        }
        else if (Integer.parseInt(value) >= 400) {
            one.setColorFilter(Color.parseColor("#FFADC8E6"));
        }

    }

    //
    @Override
    protected void onStop(){
        super.onStop();
        try{
            socket.close();
            cap_input.setText("");
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void save(View view){
        value = water_value.getText().toString();
        sp = getSharedPreferences("sp", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("value",value);
        editor.putInt("total",total);
        editor.commit();
    }

    //소켓 통신 스레드
    class ConnectThread extends Thread{
        public void run(){
            try{
                //소켓 생성
                InetAddress serverAddr = InetAddress.getByName(ip);
                socket = new Socket(serverAddr,port);
                //입력 메세지
                String sndMsg = cap_input.getText().toString();
                Log.d("...",sndMsg);
                //데이터 전송
                PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
                out.println(sndMsg);
                //데이터 수신
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String read = input.readLine();
                //출력
                mHandler.post(new msgUpdate(read));
                Log.d("...",read);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    //받은 데이터 출력
    class msgUpdate implements Runnable{
        private String msg;
        public msgUpdate(String str){
            this.msg = str;
        }
        public void run(){
            water_lv.setText(msg);
        }
    }

    //
    public void mOnC_POP(View v){
        Intent intent = new Intent(this, coffeeActivity.class);
        intent.putExtra("data", "test Popup");
        startActivityForResult(intent,1);
    }

    public void mOnG_POP(View v){
        Intent intent = new Intent(this, greenteaActivity.class);
        intent.putExtra("data", "test Popup");
        startActivityForResult(intent,1);
    }

    public void mOnB_POP(View v){
        Intent intent = new Intent(this, blackteaActivity.class);
        intent.putExtra("data", "test Popup");
        startActivityForResult(intent,1);
    }

    public void mOnM_POP(View v){
        Intent intent = new Intent(this, milkActivity.class);
        intent.putExtra("data", "test Popup");
        startActivityForResult(intent,1);
    }

}
