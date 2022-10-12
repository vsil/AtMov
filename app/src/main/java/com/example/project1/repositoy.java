package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class repositoy extends AppCompatActivity {

    private TextView[] V = new TextView[10];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositoy);

        //get textView by id into V[]
        V[0] = findViewById(R.id.textView);
        V[1] = findViewById(R.id.textView2);
        V[2] = findViewById(R.id.textView3);
        V[3] = findViewById(R.id.textView4);
        V[4] = findViewById(R.id.textView5);
        V[5] = findViewById(R.id.textView6);
        V[6] = findViewById(R.id.textView7);
        V[7] = findViewById(R.id.textView8);
        V[8] = findViewById(R.id.textView9);
        V[9] = findViewById(R.id.textView10);

        String filename = "TempStored";

        //read stored values
        FileInputStream fis = null;

        try {
            fis = getApplicationContext().openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            int c = 0;
            while (line != null) {
                if(c>10)
                    break;
                V[c].setText(line);
                c = c + 1 ;
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}