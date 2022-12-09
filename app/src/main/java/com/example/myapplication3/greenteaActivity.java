package com.example.myapplication3;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class greenteaActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_greentea);

    }


}
