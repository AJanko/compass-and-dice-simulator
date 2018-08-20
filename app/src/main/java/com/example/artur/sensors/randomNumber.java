package com.example.artur.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Random;


/**
 * Created by Artur on 2018-06-27.
 */

public class randomNumber extends AppCompatActivity implements SensorEventListener {

    private float mLastX, mLastY, mLastZ;
    private boolean mInitialized;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView shakeNumberTV;
    private final float NOISE = (float) 2.0;
    private int noshakeTimeCounter = 0;
    private int noshakeTimeCounterThreshold = 30;
    private Thread drawThread;
    private int loopIterator = 0;
    private final int loopThreshold = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_random_number);

        shakeNumberTV = findViewById(R.id.randomNumberTV);
        mInitialized = false;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onResume() {

        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onPause() {

        super.onPause();
        sensorManager.unregisterListener(this);
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // can be safely ignored for this demo
    }



    @Override
    public void onSensorChanged(SensorEvent event) {

        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        /* Pierwsze sprawdzenie sensora - inicjalizacja*/
        if (!mInitialized) {
            mLastX = x;
            mLastY = y;
            mLastZ = z;
            mInitialized = true;
        }

        /* Kolejne sprawdzenia sensora porównanie wartości*/
        else {
            float deltaX = Math.abs(mLastX - x);
            float deltaY = Math.abs(mLastY - y);
            float deltaZ = Math.abs(mLastZ - z);
            if (deltaX < NOISE || deltaY < NOISE || deltaZ < NOISE){
                if(drawThread != null)
                    noshakeTimeCounter++;   //licznik ile obiegow
            }
            else{
                noshakeTimeCounter = 0;
                if(drawThread == null)
                    initializeThread();
            }

            mLastX = x;
            mLastY = y;
            mLastZ = z;

            if(noshakeTimeCounter > noshakeTimeCounterThreshold)
                loopIterator = loopThreshold;
        }
    }


    private void initializeThread() {

        drawThread = new Thread() {
            public void run() {
                while (loopIterator++ < loopThreshold) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Random random = new Random();
                                shakeNumberTV.setText(Integer.toString(random.nextInt(6)));
                            }
                        });
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        drawThread.start();
    }
}