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
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView viewTemperature;
    private TextView viewHumidity;
    private TextView viewLuminosity;

    private Button setAlarmButton;
    private Button setRepositoryButton;
    private Button resetAlarmsButton;
    private Button resetMinMaxButton;

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

    float[] minMaxTemp = new float[2];   // [minTemp , maxTemp]
    float[] minMaxHumid = new float[2];
    float[] minMaxLuminosity = new float[2];

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


    boolean TempStoredused = false;
    float[] TempStored = new float[10]; //declarar array de floats com tamanho fixo de 10
    int TempIndex;
    Date[] TempStoredTime = new Date[10]; //declarar array de floats com tamanho fixo de 10

    boolean HumStoredused = false;
    float[] HumStored = new float[10]; //declarar array de floats com tamanho fixo de 10
    int HumIndex;
    Date[] HumStoredTime = new Date[10]; //declarar array de floats com tamanho fixo de 10

    boolean LumStoredused = false;
    float[] LumStored = new float[10]; //declarar array de floats com tamanho fixo de 10
    int LumIndex;
    Date[] LumStoredTime = new Date[10]; //declarar array de floats com tamanho fixo de 10

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
        resetAlarmsButton = findViewById(R.id.reset_alarms);
        resetAlarmsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TempAlarmSwitch.setChecked(false);
                LuminosityAlarmSwitch.setChecked(false);
                HumidAlarmSwitch.setChecked(false);
                maxTempThresh = 0f;
                minTempThresh = 0f;
                maxHumidThresh = 0f;
                minHumidThresh = 0f;
                maxLuminosityThresh = 0f;
                minLuminosityThresh = 0f;
                // show update
                // PROBLEM TO FIX :  THE THRESHOLDS ARE BEING OVERWRITTEN! NOT REALLY RESETED
                viewTempThresh.setText("min: " + minTempThresh + " ºC| Max: " + maxTempThresh + " ºC");
                viewHumidThresh.setText("min: " + minHumidThresh + "% | Max: " + maxHumidThresh + "%");
                viewLuminosityThresh.setText("min: " + minLuminosityThresh + " lx | Max: " + maxLuminosityThresh + " lx");
            }

            //ADD A SHOW METHOD
        });
        resetMinMaxButton = findViewById(R.id.reset_minmax);
        resetMinMaxButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                minMaxTemp[0] = 0f;
                minMaxTemp[1] = 0f;
                minMaxHumid[0] = 0f;
                minMaxHumid[1] = 0f;
                minMaxLuminosity[0] = 0f;
                minMaxLuminosity[1] = 0f;
                TempFirstEvent = true;
                HumidFirstEvent = true;
                LuminosityFirstEvent = true;
                //show update
                viewMinMaxTemp.setText("min: " + String.valueOf(minMaxTemp[0])+ " ºC | Max: " + String.valueOf(minMaxTemp[1])+ " ºC");
                viewMinMaxHumid.setText("min: " + String.valueOf(minMaxHumid[0])+ "% | Max: " + String.valueOf(minMaxHumid[1])+ "%");
                viewMinMaxLuminosity.setText("min: " + String.valueOf(minMaxLuminosity[0])+ " lx | Max: " + String.valueOf(minMaxLuminosity[1])+ " lx");

            }
        });

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

        //initialize times or program crashes
        for(int c=0;c<10;c++){
                TempStoredTime[c] = Calendar.getInstance().getTime();
                HumStoredTime[c] = Calendar.getInstance().getTime();
                LumStoredTime[c] = Calendar.getInstance().getTime();
        }

        //if no repository doesnt already exist create it
        File file = new File(getApplicationContext().getFilesDir(),"TempStored");
        if(!(file.exists()))
            SaveInFile("TempStored",constructString(TempIndex,TempStored,TempStoredTime));

        File file = new File(getApplicationContext().getFilesDir(),"HumStored");
        if(!(file.exists()))
            SaveInFile("HumStored",constructString(TempIndex,TempStored,TempStoredTime));
        File file = new File(getApplicationContext().getFilesDir(),"LumStored");
        if(!(file.exists()))
            SaveInFile("LumStored",constructString(TempIndex,TempStored,TempStoredTime));

    }

    public void openAlarmActivity(View view) {
        Intent intent = new Intent(this, Alarms.class);
        startActivity(intent);
    }

    public void openRepository(View view) {
        //open new activity that shows repository
        Intent intent = new Intent(this, Repository.class);
        startActivity(intent);
    }

    public String constructString(int index,float[] stored,Date[] time){
        String txt = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");//this is used to transform time into a string for hours minutes and seconds

        int c=index+1;
        txt = txt + simpleDateFormat.format(time[c]) + ": " + String.valueOf(stored[index]) + "\n" ;
        while(c != index){
            txt = txt + simpleDateFormat.format(time[c]) + ": " + String.valueOf(stored[c]) + "\n" ;
            c=c+1;
            if (c == 10) //return to the begining
                c = 0; //round robin
        }
        return txt;
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
                TempStoredused = true;
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
                //save up to 10 values
                HumStoredused = true;
                HumStored[HumIndex]=event.values[0];
                HumStoredTime[HumIndex]= Calendar.getInstance().getTime();//get time stamp

                HumIndex = HumIndex + 1; //next measurement must be in the next position
                if (HumIndex == 10) //return to the begining
                    HumIndex = 0; //round robin
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

                //save up to 10 values
                LumStoredused = true;
                LumStored[LumIndex]=event.values[0];
                LumStoredTime[LumIndex]= Calendar.getInstance().getTime();//get time stamp

                LumIndex = LumIndex + 1; //next measurement must be in the next position
                if (LumIndex == 10) //return to the begining
                    LumIndex = 0; //round robin

                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i){
        String msg = "";
        String sensorType;
        switch(sensor.getType()){
            case Sensor.TYPE_LIGHT:
                sensorType = "Light";
                break;

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                sensorType = "Temperature";
                break;

            case Sensor.TYPE_RELATIVE_HUMIDITY:
                sensorType = "Humidity";
                break;
            default:
                sensorType = "";
        }

        switch(i){
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH:
                msg= sensorType + " Sensor has high accuracy";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM:
                msg= sensorType + " Sensor has medium accuracy";
                break;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW:
                msg= sensorType + " Sensor has low accuracy";
                break;
            case SensorManager.SENSOR_STATUS_UNRELIABLE:
                msg=sensorType + " Sensor has unreliable accuracy";
                break;
            default:
                break;
        }
        Toast accuracyToast = Toast.makeText(this.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        accuracyToast.show();
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

        // recover saved Temperature state: threshold, state of alarm (ON/OFF), and minimum and Maximum registered
        minTempThresh = sh.getFloat("min_temp_thresh", 0);
        maxTempThresh = sh.getFloat("max_temp_thresh", 0);
        viewTempThresh.setText("min: " + minTempThresh + " ºC| Max: " + maxTempThresh + " ºC");
        TempAlarmSwitch.setChecked(sh.getBoolean("temp_switch_checked",false));
        minMaxTemp[0]=sh.getFloat("min_temp", 0);
        minMaxTemp[1]=sh.getFloat("max_temp", 0);
        viewMinMaxTemp.setText("min: " + String.valueOf(minMaxTemp[0])+ " ºC | Max: " + String.valueOf(minMaxTemp[1])+ " ºC");

        // recover saved Humidity state
        minHumidThresh = sh.getFloat("min_humid_thresh", 0);
        maxHumidThresh = sh.getFloat("max_humid_thresh", 0);
        viewHumidThresh.setText("min: " + minHumidThresh + "% | Max: " + maxHumidThresh + "%");
        HumidAlarmSwitch.setChecked(sh.getBoolean("humid_switch_checked",false));
        minMaxHumid[0]=sh.getFloat("min_humid", 0);
        minMaxHumid[1]=sh.getFloat("max_humid", 0);
        viewMinMaxHumid.setText("min: " + String.valueOf(minMaxHumid[0])+ "% | Max: " + String.valueOf(minMaxHumid[1])+ "%");


        // recover saved Luminosity state
        minLuminosityThresh = sh.getFloat("min_luminosity_thresh", 0);
        maxLuminosityThresh = sh.getFloat("max_luminosity_thresh", 0);
        viewLuminosityThresh.setText("min: " + minLuminosityThresh + " lx | Max: " + maxLuminosityThresh + " lx");
        LuminosityAlarmSwitch.setChecked(sh.getBoolean("luminosity_switch_checked",false));
        minMaxLuminosity[0]=sh.getFloat("min_luminosity", 0);
        minMaxLuminosity[1]=sh.getFloat("max_luminosity", 0);
        viewMinMaxLuminosity.setText("min: " + String.valueOf(minMaxLuminosity[0])+ " lx | Max: " + String.valueOf(minMaxLuminosity[1])+ " lx");

        //recover stored values

    }

    @Override
    protected void onPause() {

        // saves alarm state;
        SharedPreferences pref = getApplicationContext().getSharedPreferences("SharedPreferences", 0); // 0 - for private mode
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("temp_switch_checked", TempAlarmSwitch.isChecked());
        editor.putBoolean("humid_switch_checked", HumidAlarmSwitch.isChecked());
        editor.putBoolean("luminosity_switch_checked", LuminosityAlarmSwitch.isChecked());

        saveMinMax("temp", minMaxTemp, editor);
        saveMinMax("humid", minMaxHumid, editor);
        saveMinMax("luminosity", minMaxLuminosity, editor);

        editor.commit();

        if(TempStoredused)
            SaveInFile("TempStored",constructString(TempIndex,TempStored,TempStoredTime));
        if(HumStoredused)
            SaveInFile("HumStored",constructString(HumIndex,HumStored,HumStoredTime));
        if(LumStoredused)
            SaveInFile("LumStored",constructString(LumIndex,LumStored,LumStoredTime));

        super.onPause();
        sensorManager.unregisterListener(this);   // sensors unregister
    }

    private void saveMinMax(String variable, float[] minMax, SharedPreferences.Editor editor) {
        if(minMax[0] == 0.0f){
            // if no sensor values were changed on the emulator, minMax is null so we save a default 0 for minMax
            editor.putFloat("min_"+variable, 0);
            editor.putFloat("max_"+variable, 0);
        }
        else{
            editor.putFloat("min_"+variable, minMax[0]);
            editor.putFloat("max_"+variable, minMax[1]);
        }

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

