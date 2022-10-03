package com.example.project1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView viewTemperature;
    private TextView viewPressure;

    private TextView viewMinTemp;
    private TextView viewMaxTemp;

    private Sensor temperatureSensor;
    private Sensor pressureSensor;

    private boolean isTempSensorAvailable;
    private boolean isPressureSensorAvailable;

    private SensorManager sensorManager;

    float[] minMaxTemp;   // [minTemp , maxTemp]

    boolean TempFirstEvent;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewTemperature = findViewById(R.id.view_temperature);
        viewPressure = findViewById(R.id.view_pressure);

        viewMinTemp = findViewById(R.id.viewMinTemp);
        viewMaxTemp = findViewById(R.id.viewMaxTemp);

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

        if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
            pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            isPressureSensorAvailable = true;
            Log.i("ISPress?", "pressure exists");

        } else {
            viewTemperature.setText("404: Temp Sensor not available");
            isPressureSensorAvailable = false;
            Log.i("ISPress?", "press doesnt exist");
        }



    }

    // Question: Only shows values after they are changed by hand;
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()){

            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                viewTemperature.setText(event.values[0] + " ºC");
                Log.i(" Broke?", "ON1");

                minMaxTemp = checkMinMax(event.values[0], minMaxTemp, TempFirstEvent);
                Log.i(" Broke?", "ON2");

                viewMinTemp.setText("min: " + String.valueOf(minMaxTemp[0])+ " ºC");
                viewMaxTemp.setText("Max: " + String.valueOf(minMaxTemp[1])+ " ºC");  // perhaps put this inside checkMinMax()

                TempFirstEvent = false;          // can use a if True, para nao executar em todos os eventos

                break;

            case Sensor.TYPE_PRESSURE:
                viewPressure.setText(event.values[0] + " hPa");
                Log.i("PRESSEVENT?", "ON");
                break;
        }
/*
        if(event.sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE){    //later implement switch
            viewTemperature.setText(event.values[0] + " ºC");
            Log.i("TEMPEVENT?", "ON");
        }

 */
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
        if (isPressureSensorAvailable) {
            sensorManager.registerListener(this, pressureSensor, sensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTempSensorAvailable) {
            sensorManager.unregisterListener(this);    // is this correct?
        }
        if (isPressureSensorAvailable) {
            sensorManager.unregisterListener(this);   // is it one unregister for both?
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
}

