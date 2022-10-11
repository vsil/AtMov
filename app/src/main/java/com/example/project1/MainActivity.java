package com.example.project1;

import static com.example.project1.Notifications.CHANNEL_1_ID;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.temporal.Temporal;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView viewTemperature;
    private TextView viewHumidity;
    private TextView viewLuminosity;

    private Button setAlarmButton;

    private TextView viewMinMaxTemp;

    private TextView viewTempThresh;
    private Switch TempAlarmSwitch;
    private Button setRepositoryButton;

    private TextView viewMinMaxHumid;
    private TextView viewHumidThresh;
    private Switch HumidAlarmSwitch;

    private TextView viewMinMaxLuminosity;
    private TextView viewLuminosityThresh;
    private Switch LuminosityAlarmSwitch;

    private Sensor temperatureSensor;
    private Sensor humiditySensor;
    private Sensor luminositySensor;

    private boolean isTempSensorAvailable;
    private boolean isHumidySensorAvailable;
    private boolean isLuminositySensorAvailable;

    private SensorManager sensorManager;

    float[] minMaxTemp;   // [minTemp , maxTemp]
    float[] minMaxHumid;
    float[] getMinMaxTemp;

    boolean TempFirstEvent;
    boolean HumidFirstEvent;
    boolean LuminFirstEvent;

    float[] TempThreshold;      // defined by user, who sets alarms
    float[] LuminosityThreshold; // [min, max]
    float[] HumidityThreshold;

    SharedPreferences sh;
    private NotificationManagerCompat notificationManager;

    Float minTempThresh;
    Float maxTempThresh;

    float[] TempStored = new float[10]; //declarar array de floats com tamanho fixo de 10

    int TempIndex;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationManager = NotificationManagerCompat.from(this);

        setContentView(R.layout.activity_main);


        viewTemperature = findViewById(R.id.view_temperature);
        viewHumidity = findViewById(R.id.view_humidity);
        viewLuminosity = findViewById(R.id.view_luminosity);

        viewMinMaxTemp = findViewById(R.id.viewMinMaxTemp);
        viewTempThresh = findViewById(R.id.viewTempThresh);
        TempAlarmSwitch = findViewById(R.id.TempAlarmSwitch);
        TempAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clicking on switch opens alarm activities if neither of the alarm values are set
                if(minTempThresh==null||maxTempThresh==null){
                    Intent intent = new Intent(view.getContext(), Alarms.class);
                    startActivity(intent);
                }
            }
        });

        viewMinMaxHumid = findViewById(R.id.viewMinMaxHumid);
        viewHumidThresh = findViewById(R.id.viewHumidThreshold);
        HumidAlarmSwitch = findViewById(R.id.HumidityAlarmSwitch);
//set onclick!!       TempAlarmSwitch.setOnClickListener(new View.OnClickListener() {};

        viewMinMaxLuminosity = findViewById(R.id.viewMinMaxLuminosity);
        viewLuminosityThresh = findViewById(R.id.viewLuminosityThreshold);
        LuminosityAlarmSwitch = findViewById(R.id.LuminosityAlarmSwitch);
/*
        LuminosityAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
*/


        setAlarmButton = findViewById(R.id.set_alarm);
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlarmActivity();
            }
        });//da para fazer isto mais facilmente no xml file

        setRepositoryButton = findViewById(R.id.set_repository);
        setRepositoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRepository();
            }
        });//da para fazer isto mais facilmente no xml file

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // put all this in a function ; get_sensors() or something
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isTempSensorAvailable = true;
            TempFirstEvent = true;         //becomes false after first temp sensor reading
            //Log.i("ISTEMP?", "temp exists");
            TempIndex = 0;

        } else {
            viewTemperature.setText("404: Temp Sensor not available");
            isTempSensorAvailable = false;
            //Log.i("ISTEMP?", "temp doesnt exist");
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            isHumidySensorAvailable = true;

        } else {
            viewHumidity.setText("404: Humidity Sensor not available");
            isHumidySensorAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            luminositySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            isLuminositySensorAvailable = true;

        } else {
            viewLuminosity.setText("404: Luminosity Sensor not available");
            isLuminositySensorAvailable = false;
        }

        //create file to store values
        String filename = "TempStored";
        File file = new File(getApplicationContext().getFilesDir(), filename);
    }

    private void openAlarmActivity() {
        Intent intent = new Intent(this, Alarms.class);
        startActivity(intent);
    }

    private void openRepository() {
        String txt = "";
        for(int c=0;c<10;c=c+1){
            txt = txt + String.valueOf(TempStored[c]) + "\n" ;
        }
        String filename = "TempStored";

        //store values
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(txt.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, repositoy.class);
        startActivity(intent);
    }

    // Question: Only shows values after they are changed by hand;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                viewTemperature.setText(event.values[0] + " ºC");
                minMaxTemp = checkMinMax(event.values[0], minMaxTemp, TempFirstEvent);
                viewMinMaxTemp.setText("min: " + String.valueOf(minMaxTemp[0])+ " ºC | Max: " + String.valueOf(minMaxTemp[1])+ " ºC");

                TempFirstEvent = false;          // can use a if True, para nao executar em todos os eventos

                if(TempAlarmSwitch.isChecked()) {

                    // function check_threshold()
                    if (event.values[0] < minTempThresh) {
                        Log.i(" Temp Threshold", "ON2");

                        // create intent to open main activity after use click on alarm notification
                        Intent myIntent = new Intent(this, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                this,
                                0,
                                myIntent,
                                PendingIntent.FLAG_IMMUTABLE);

                        // send notification
                        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                                .setSmallIcon(R.drawable.ic_message)
                                .setContentTitle("Temperature Alert")
                                .setContentText("Minimum temperature Alert")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_ALARM)
                                .setContentIntent(pendingIntent)
                                .setAutoCancel(true)
                                .build();
                        notificationManager.notify(1, notification);
                    }
                    if (event.values[0] > maxTempThresh) {
                        Log.i(" Temp Threshold MAX", "ON2");

                        //create intent to open main activity
                        Intent myIntent = new Intent(this, MainActivity.class);
                        PendingIntent pendingIntent = PendingIntent.getActivity(
                                this,
                                0,
                                myIntent,
                                PendingIntent.FLAG_IMMUTABLE);

                        // send notification
                        Notification notification = new NotificationCompat.Builder(this, CHANNEL_1_ID)
                                .setSmallIcon(R.drawable.ic_message)
                                .setContentTitle("Temperature Alert")
                                .setContentText("Maximum temperature Alert")
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setCategory(NotificationCompat.CATEGORY_ALARM)
                                .setAutoCancel(true)
                                .build();
                        notificationManager.notify(1, notification);
                    }
                }

                //save up to 10 values
                TempStored[TempIndex]=event.values[0];

                TempIndex = TempIndex + 1; //next measurement must be in the next position
                if (TempIndex == 10) //return to the begining
                    TempIndex = 0; //round robin

                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                viewHumidity.setText(event.values[0] + "%");
                break;

            case Sensor.TYPE_LIGHT:
                viewLuminosity.setText(event.values[0] + " lx");
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isTempSensorAvailable) {
            sensorManager.registerListener(this, temperatureSensor, sensorManager.SENSOR_DELAY_NORMAL);
        }

        if (isLuminositySensorAvailable) {
            sensorManager.registerListener(this, luminositySensor, sensorManager.SENSOR_DELAY_NORMAL);
        }
        if (isHumidySensorAvailable) {
            sensorManager.registerListener(this, humiditySensor, sensorManager.SENSOR_DELAY_NORMAL);
        }


        sh = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        minTempThresh = sh.getFloat("min_temp_thresh", 0);
        maxTempThresh = sh.getFloat("max_temp_thresh", 0);
        TempAlarmSwitch.setChecked(sh.getBoolean("alarm_switch_checked",false));

        Log.i(" SHARED PREFS READING: ", String.valueOf(minTempThresh));
        viewTempThresh.setText("min: " + minTempThresh + " ºC | Max: " + maxTempThresh + " ºC");

    }

    @Override
    protected void onPause() {

        // saves alarm state;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("SharedPreferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("alarm_switch_checked", TempAlarmSwitch.isChecked()); // Storing float
        editor.commit();

        super.onPause();
        sensorManager.unregisterListener(this);   // is it one unregister for all sensors?
    }

    // esta funcao consegue aceder ao minMax? se sim, nao tenho de passar como argumento
    protected float[] checkMinMax(float event, float[] minMax, boolean FirstEvent){
        float[] newMinMax = new float[2];

        if(FirstEvent){
            newMinMax[0] = event;
            newMinMax[1] = event;
            return newMinMax;
        }

        newMinMax = minMax.clone();
        if(event<minMax[0]){
            newMinMax[0] = event;
        }
        if(event>minMax[1]){
            newMinMax[1] = event;
        }
        return newMinMax;
    }

}

