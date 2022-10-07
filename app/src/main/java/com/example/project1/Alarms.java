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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("SharedPreferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();

        minTempButton = findViewById(R.id.TempMinButton);
        minTempInput = findViewById(R.id.TempMinInput);
        minTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editor.putFloat("min_temp_thresh", Float.parseFloat(minTempInput.getText().toString())); // Storing float
                editor.commit();

            }
        });
    }


}