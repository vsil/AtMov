package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Alarms extends AppCompatActivity {


    private Button minTempButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);

        minTempButton = findViewById(R.id.TempMinButton);
        minTempButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // send edit text value
                String value="Hello world";
                Intent i = new Intent(CurrentActivity.this, NewActivity.class);
                i.putExtra("key",value);
                startActivity(i);
            }
        });

        // then on mainactivity - check where it should be put
        /*
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("key");
            //The key argument here must match that used in the other activity
        }
        
         */
    }


}