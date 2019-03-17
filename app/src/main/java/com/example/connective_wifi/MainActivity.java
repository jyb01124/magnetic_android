package com.example.connective_wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magSensor;
    int rotation;
    TextView tvXaxis, tvYaxis, tvZaxis, tvMagnetic;
    int fail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button Send_button = (Button) findViewById(R.id.send_button);

        Send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Send 버튼이 눌려졌음", Toast.LENGTH_LONG).show();
            }
        });

        tvXaxis = (TextView)findViewById(R.id.tvXaxis);
        tvYaxis = (TextView)findViewById(R.id.tvYaxis);
        tvZaxis = (TextView)findViewById(R.id.tvZaxis);
        tvMagnetic = (TextView)findViewById(R.id.tvMagnetic);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @SuppressLint("StaticFieldLeak")
    public void onToggleClicked(View v) {

        Button Connect_button = (Button) findViewById(R.id.connect_button);

        boolean on = ((ToggleButton) v).isChecked();
        if (on) {
            fail = 0;
            try {
                new AsyncTask<Integer, Void, Integer>() {
                    @Override
                    protected Integer doInBackground(Integer... params) {
                        try {
                            executeRemoteCommand("ybcho", "gagaseoro1@", "192.168.0.101", 22);
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail = 1;
                        }
                        return fail;
                    }
                }.execute(1).get();
            } catch (Exception f) {
                f.printStackTrace();
            }
            if (fail == 0) {
                Toast.makeText(getApplicationContext(), "serverON", Toast.LENGTH_LONG).show();
                Connect_button.setVisibility(View.VISIBLE);

                Connect_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getApplicationContext(), "Connect 버튼이 눌려졌음", Toast.LENGTH_LONG).show();
                    }
                });

            }else{
                    Toast.makeText(getApplicationContext(), "Server Error", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "Please rebuild", Toast.LENGTH_LONG).show();
                }

        } else {
            Toast.makeText(getApplicationContext(), "unchecked", Toast.LENGTH_LONG).show();
            Connect_button.setVisibility(View.INVISIBLE);
        }
    }

    public static String executeRemoteCommand(String username,String password,String hostname,int port)
            throws Exception {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, hostname, port);
        session.setPassword(password);

        // Avoid asking for key confirmation
        Properties prop = new Properties();
        prop.put("StrictHostKeyChecking", "no");
        session.setConfig(prop);

        session.connect();

        // SSH Channel
        ChannelExec channelssh = (ChannelExec)
                session.openChannel("exec");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        channelssh.setOutputStream(baos);

        // Execute command
        channelssh.setCommand("lsusb > /home/pi/test.txt");
        channelssh.connect();
        channelssh.disconnect();

        return baos.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor ==magSensor) {
            float magX = event.values[0];
            float magY = event.values[1];
            float magZ = event.values[2];
            double magnitude = Math.sqrt((magX * magX) + (magY * magY) + (magZ * magZ));

            tvXaxis.setText("X axis : " + String.format("%.2f", magX));
            tvYaxis.setText("Y axis : " + String.format("%.2f", magY));
            tvZaxis.setText("Z axis : " + String.format("%.2f", magZ));
            tvMagnetic.setText("Magnetic : "  + String.format("%.2f", magnitude) + " \u00B5Tesla");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}




