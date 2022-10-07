package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.TextView;

import java.io.Console;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView viewTemperature;
    private TextView viewHumidity;
    private TextView viewLuminosity;

    private Button setAlarmButton;
    private TextView viewMinMaxTemp;


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




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewTemperature = findViewById(R.id.view_temperature);
        viewHumidity = findViewById(R.id.view_humidity);
        viewLuminosity = findViewById(R.id.view_luminosity);

        viewMinMaxTemp = findViewById(R.id.viewMinMaxTemp);

        setAlarmButton = findViewById(R.id.set_alarm);
        setAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openAlarmActivity();
            }
        });
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // put all this in a function ; get_sensors() or something
        if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
            temperatureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            isTempSensorAvailable = true;
            TempFirstEvent = true;         //becomes false after first temp sensor reading
            //Log.i("ISTEMP?", "temp exists");

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

    }

    private void openAlarmActivity() {
        Intent intent = new Intent(this, Alarms.class);
        startActivity(intent);
    }

    // Question: Only shows values after they are changed by hand;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                viewTemperature.setText(event.values[0] + " ºC");

                minMaxTemp = checkMinMax(event.values[0], minMaxTemp, TempFirstEvent);
                Log.d("min: " , "min: " + String.valueOf(minMaxTemp[0]));
                Log.d("max: ", "max: " + String.valueOf(minMaxTemp[1]));
                viewMinMaxTemp.setText("min: " + String.valueOf(minMaxTemp[0])+ " ºC | Max: " + String.valueOf(minMaxTemp[1])+ " ºC");

                TempFirstEvent = false;          // can use a if True, para nao executar em todos os eventos
/*
                // function check_threshold()
                if(event.values[0]<TempThreshold[0] || event.values[0]>TempThreshold[1]){
                    Log.i(" Temp Threshold", "ON2");
                    // send notification
                }

 */
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


        SharedPreferences sh = getSharedPreferences("SharedPreferences", Context.MODE_PRIVATE);
        Float minTempThresh = sh.getFloat("min_temp_thresh", 0);
        Log.i(" SHARED PREFS READING: ", String.valueOf(minTempThresh));
        viewTemperature.setText(minTempThresh + " ºC");


    }

    @Override
    protected void onPause() {
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

