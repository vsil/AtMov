package com.example.project1;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class Alarms extends AppCompatActivity {


    private Button minTempButton;
    private TextView minTempInput;
    private Button maxTempButton;
    private TextView maxTempInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("SharedPreferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();


        minTempInput = findViewById(R.id.TempMinInput);
        minTempButton = findViewById(R.id.TempMinButton);
        minTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putFloat("min_temp_thresh", Float.parseFloat(minTempInput.getText().toString())); // Storing float
                editor.commit();

            }
        });
        maxTempInput = findViewById(R.id.TempMaxInput);
        maxTempButton = findViewById(R.id.TempMaxButton);
        maxTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putFloat("max_temp_thresh", Float.parseFloat(maxTempInput.getText().toString())); // Storing float
                editor.commit();

            }
        });
    }


}