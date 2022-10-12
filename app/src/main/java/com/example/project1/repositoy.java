package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class repositoy extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repositoy);

        fill("TemptextView","TempStored");
        fill("HumtextView","HumStored");
        fill("LumtextView","LumStored");


}

public void fill (String tag,String filename){

    TextView[] V = new TextView[10];
    //get textView by id into V[]
    for (int i=0;i<10;i++){
        String name = tag + Integer.toString(i+1);
        int id = getResources().getIdentifier(name, "id", getApplicationContext().getPackageName());
        V[i] = findViewById(id);
    }



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