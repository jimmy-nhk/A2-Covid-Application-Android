package com.example.a2.activity;

import com.example.a2.R;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Intent intent = getIntent();
        ArrayList<String> hello = intent.getStringArrayListExtra("tmp");

        for (String s : hello){
            System.out.println("name: " + s);
        }
    }
}