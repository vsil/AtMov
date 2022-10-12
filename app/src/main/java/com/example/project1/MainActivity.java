package com.example.project1;

import static com.example.project1.Notifications.CHANNEL_1_ID;
import static com.example.project1.Notifications.CHANNEL_2_ID;
import static com.example.project1.Notifications.CHANNEL_3_ID;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;



public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView viewTemperature;
    private TextView viewHumidity;
    private TextView viewLuminosity;

    private Button setAlarmButton;
    private Button setRepositoryButton;

    private TextView viewMinMaxTemp;
    private TextView viewTempThresh;
    private Switch TempAlarmSwitch;

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
    private boolean isHumiditySensorAvailable;
    private boolean isLuminositySensorAvailable;

    float[] minMaxTemp;   // [minTemp , maxTemp]
    float[] minMaxHumid;
    float[] minMaxLuminosity;

    boolean TempFirstEvent;
    boolean HumidFirstEvent;
    boolean LuminosityFirstEvent;

    Float minTempThresh;
    Float maxTempThresh;
    Float minHumidThresh;
    Float maxHumidThresh;
    Float minLuminosityThresh;
    Float maxLuminosityThresh;

    private SensorManager sensorManager;
    SharedPreferences sh;
    private NotificationManagerCompat notificationManager;



    float[] TempStored = new float[10]; //declarar array de floats com tamanho fixo de 10
    int TempIndex;
    Date[] TempStoredTime = new Date[10]; //declarar array de floats com tamanho fixo de 10

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
                if(minTempThresh==0&maxTempThresh==0){
                    Intent intent = new Intent(view.getContext(), Alarms.class);
                    startActivity(intent);
                }
            }
        });

        viewMinMaxHumid = findViewById(R.id.viewMinMaxHumid);
        viewHumidThresh = findViewById(R.id.viewHumidThreshold);
        HumidAlarmSwitch = findViewById(R.id.HumidityAlarmSwitch);
        HumidAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clicking on switch opens alarm activities if neither of the alarm values are set
                if(minHumidThresh==0||maxHumidThresh==0){
                    Intent intent = new Intent(view.getContext(), Alarms.class);
                    startActivity(intent);
                }
            }
        });

        viewMinMaxLuminosity = findViewById(R.id.viewMinMaxLuminosity);
        viewLuminosityThresh = findViewById(R.id.viewLuminosityThreshold);
        LuminosityAlarmSwitch = findViewById(R.id.LuminosityAlarmSwitch);
        LuminosityAlarmSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // clicking on switch opens alarm activities if neither of the alarm values are set
                if(minLuminosityThresh==0||maxLuminosityThresh==0){
                    Intent intent = new Intent(view.getContext(), Alarms.class);
                    startActivity(intent);
                }
            }
        });


        setAlarmButton = findViewById(R.id.set_alarm);
        setRepositoryButton = findViewById(R.id.set_repository);


        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // put all this in a function ; get_sensors() or something
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isTempSensorAvailable = true;
            TempFirstEvent = true;         //becomes false after first temp sensor reading
            TempIndex = 0;

        } else {
            viewTemperature.setText("404: Temp Sensor not available");
            isTempSensorAvailable = false;

        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
            humiditySensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
            isHumiditySensorAvailable = true;
            HumidFirstEvent = true;

        } else {
            viewHumidity.setText("404: Humidity Sensor not available");
            isHumiditySensorAvailable = false;
        }

        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            luminositySensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            isLuminositySensorAvailable = true;
            LuminosityFirstEvent = true;

        } else {
            viewLuminosity.setText("404: Luminosity Sensor not available");
            isLuminositySensorAvailable = false;
        }

        //create file to store values
        String filename = "TempStored";
        File file = new File(getApplicationContext().getFilesDir(), filename);

        //initialize times or program crashes
        for(int c=0;c<10;c++)
            TempStoredTime[c]= Calendar.getInstance().getTime();
    }

    public void openAlarmActivity(View view) {
        Intent intent = new Intent(this, Alarms.class);
        startActivity(intent);
    }

    public void openRepository(View view) {
        //store sensor data in file
        //build string to store
        String txt = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");//this is used to transform time into a string for hours minutes and seconds

        int c=TempIndex+1;
        txt = txt + simpleDateFormat.format(TempStoredTime[c]) + ":" + String.valueOf(TempStored[TempIndex]) + "\n" ;
        while(c != TempIndex){
            txt = txt + simpleDateFormat.format(TempStoredTime[c]) + ":" + String.valueOf(TempStored[c]) + "\n" ;
            c=c+1;
            if (c == 10) //return to the begining
                c = 0; //round robin
        }

        SaveInFile("TempStored",txt);

        //open new activity that shows repository
        Intent intent = new Intent(this, repositoy.class);
        startActivity(intent);
    }

    public void SaveInFile(String filename,String content){
        //store values in [filename]
        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) { //everytime I do this the file gets deleted cleared and if I try append mode it crashes
            fos.write(content.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String ReadFile(String filename){
        //Returns a string with the contents of [filename]
        FileInputStream fis = null;

        try {
            fis = getApplicationContext().openFileInput(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
        } finally {
            String contents = stringBuilder.toString();
            return contents;
        }
}

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
                        sendNotificationAlert(CHANNEL_1_ID, "Minimum", "Temperature");

                    }
                    if (event.values[0] > maxTempThresh) {
                        Log.i(" Temp Threshold MAX", "ON2");
                        sendNotificationAlert(CHANNEL_1_ID, "Maximum", "Temperature");
                    }
                }

                //save up to 10 values
                TempStored[TempIndex]=event.values[0];
                TempStoredTime[TempIndex]= Calendar.getInstance().getTime();//get time stamp

                TempIndex = TempIndex + 1; //next measurement must be in the next position
                if (TempIndex == 10) //return to the begining
                    TempIndex = 0; //round robin

                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                viewHumidity.setText(event.values[0] + "%");
                minMaxHumid = checkMinMax(event.values[0], minMaxHumid, HumidFirstEvent);
                viewMinMaxHumid.setText("min: " + String.valueOf(minMaxHumid[0])+ "% | Max: " + String.valueOf(minMaxHumid[1])+ "%");

                HumidFirstEvent = false;          // can use a if True, para nao executar em todos os eventos

                if(HumidAlarmSwitch.isChecked()) {

                    // function check_threshold()
                    if (event.values[0] < minHumidThresh) {
                        sendNotificationAlert(CHANNEL_2_ID, "Minimum", "Humidity");

                    }
                    if (event.values[0] > maxHumidThresh) {
                        sendNotificationAlert(CHANNEL_2_ID, "Maximum", "Humidity");
                    }
                }
                break;

            case Sensor.TYPE_LIGHT:
                viewLuminosity.setText(event.values[0] + "%");
                minMaxLuminosity = checkMinMax(event.values[0], minMaxLuminosity, LuminosityFirstEvent);
                viewMinMaxLuminosity.setText("min: " + String.valueOf(minMaxLuminosity[0])+ " lx | Max: " + String.valueOf(minMaxLuminosity[1])+ " lx");

                LuminosityFirstEvent = false;          // can use a if True, para nao executar em todos os eventos

                if(LuminosityAlarmSwitch.isChecked()) {

                    if (event.values[0] < minLuminosityThresh) {
                        sendNotificationAlert(CHANNEL_3_ID, "Minimum", "Luminosity");

                    }
                    if (event.values[0] > maxLuminosityThresh) {
                        sendNotificationAlert(CHANNEL_3_ID, "Maximum", "Luminosity");
                    }
                }
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
        if (isHumiditySensorAvailable) {
            sensorManager.registerListener(this, humiditySensor, sensorManager.SENSOR_DELAY_NORMAL);
        }

        sh = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);

        minTempThresh = sh.getFloat("min_temp_thresh", 0);
        maxTempThresh = sh.getFloat("max_temp_thresh", 0);
        viewTempThresh.setText("min: " + minTempThresh + " ºC| Max: " + maxTempThresh + " ºC");
        TempAlarmSwitch.setChecked(sh.getBoolean("temp_switch_checked",false));

        minHumidThresh = sh.getFloat("min_humid_thresh", 0);
        maxHumidThresh = sh.getFloat("max_humid_thresh", 0);
        viewHumidThresh.setText("min: " + minHumidThresh + "% | Max: " + maxHumidThresh + "%");
        HumidAlarmSwitch.setChecked(sh.getBoolean("humid_switch_checked",false));

        minLuminosityThresh = sh.getFloat("min_luminosity_thresh", 0);
        maxLuminosityThresh = sh.getFloat("max_luminosity_thresh", 0);
        viewLuminosityThresh.setText("min: " + minLuminosityThresh + " lx | Max: " + maxLuminosityThresh + " lx");
        LuminosityAlarmSwitch.setChecked(sh.getBoolean("luminosity_switch_checked",false));

        //add portion to put the repository back in the sored vector
    }

    @Override
    protected void onPause() {

        // saves alarm state;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("SharedPreferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("temp_switch_checked", TempAlarmSwitch.isChecked());
        editor.putBoolean("humid_switch_checked", HumidAlarmSwitch.isChecked());
        editor.putBoolean("luminosity_switch_checked", LuminosityAlarmSwitch.isChecked());
        editor.commit();

        super.onPause();
        sensorManager.unregisterListener(this);   // sensors unregister
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


    public void sendNotificationAlert(String Channel_ID, String AlarmType, String Variable){
        // create intent to open main activity after use click on alarm notification
        Intent myIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                myIntent,
                PendingIntent.FLAG_IMMUTABLE);

        // send notification
        Notification notification = new NotificationCompat.Builder(this, Channel_ID)
                .setSmallIcon(R.drawable.ic_message)
                .setContentTitle(Variable + " Alert")
                .setContentText(AlarmType + " " + Variable + " Alert")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        notificationManager.notify(1, notification);
    };

}

