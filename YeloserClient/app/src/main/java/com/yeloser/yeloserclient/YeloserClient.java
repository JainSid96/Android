package com.yeloser.yeloserclient;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yeloser.yeloserclient.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class YeloserClient extends Activity implements SensorEventListener {
    private BluetoothAdapter mBluetoothAdapter = null;
    private volatile BluetoothSocket mSocket = null;
    Thread mMessageListener = null;
    InputStream mInStream = null;

    private UUID uuid = UUID.fromString("00E105EA-0000-1000-8000-00805F9B34FB");

    private ArrayList<BluetoothDevice> foundDevices = new ArrayList<>();

    private ArrayAdapter<BluetoothDevice> aa;
    private ListView list;

    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    private static final int SHAKE_THRESHOLD = 600;
    private static long lastUpdate = System.currentTimeMillis();
    private float lastX, lastY, lastZ;

    // INNER CLASSES

    private class MessagePoster implements Runnable {
        private TextView textView;
        private String message;

        public MessagePoster(TextView textView, String message) {
            this.textView = textView;
            this.message = message;
        }

        public void run() {
            if (message.equals("stop")) {
                Log.i("", "Stopping All");
                sendMessage(message);
                exit();
            }
            textView.setText(message);
        }
    }


    private class BluetoothSocketListener implements Runnable {
        private TextView textView;

        public BluetoothSocketListener(BluetoothSocket socket, TextView textView) {
            mSocket = socket;
            this.textView = textView;
        }

        public void run() {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            try {
                mInStream = mSocket.getInputStream();

                StringBuilder message = new StringBuilder();
                while (true) {
                    message.setLength(0);
                    int bytesRead = mInStream.read(buffer);
                    if (bytesRead != -1) {
                        while ((bytesRead == bufferSize) && (buffer[bufferSize - 1] != 0)) {
                            message.append(new String(buffer, 0, bytesRead));
                            bytesRead = mInStream.read(buffer);
                        }
                        message.append(new String(buffer, 0, bytesRead - 1));
                        new Handler(Looper.getMainLooper()).post(new MessagePoster(textView, message.toString()));

                        if (message.toString().equals("stop")) break;
                    }
                }
            } catch (IOException e) {
                Log.e("", "" + e);
                exit();
            }
        }
    }

    BluetoothSocketListener mSocketListener = null;

    private boolean mReceiverRegistered = false;

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (mBluetoothAdapter.getBondedDevices().contains(remoteDevice)) {
                foundDevices.add(remoteDevice);
                aa.notifyDataSetChanged();
            }
        }
    };

    // METHODS

    private void exit() {
        Log.i("*****", "Begin");

        if (mReceiverRegistered) {
            unregisterReceiver(discoveryResult);
            mReceiverRegistered = false;
        }

        try {
            if (mInStream != null) {
                mInStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
            }
        }
        catch (IOException e) { Log.e("", "" + e); }

        senSensorManager.unregisterListener(this);

        Log.i("*****", "End");
    }


    private void setupAdapter() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i("", "Bluetooth Adapter Name: " + mBluetoothAdapter.getName());
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eloser_client);

        int [] infoIds = { R.id.info0, R.id.info1, R.id.info2, R.id.info3, R.id.info4 };

        Log.initLog(this, infoIds, Log.LogLevel.Verbose);

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        setupAdapter();
        setupListView();

        if (!mReceiverRegistered) {
            registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            mReceiverRegistered = true;
        }

        if (!mBluetoothAdapter.isDiscovering()) {
            foundDevices.clear();
            mBluetoothAdapter.startDiscovery();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eloser_client, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    protected void onResume() {
        super.onResume();

        Log.i("*****", "Begin");

        //senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        Log.i("*****", "End");
    }


    @Override
    protected void onPause() {
        super.onPause();

        Log.i("*****", "Begin");

        //senSensorManager.unregisterListener(this);

        Log.i("*****", "End");
    }


    @Override
    protected void onDestroy() {
        Log.i("*****", "Begin");

        super.onDestroy();

        exit();

        Log.i("*****", "End");
    }


    private void setupListView() {
        aa = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, foundDevices);
        list = (ListView) findViewById(R.id.list_discovered);
        list.setAdapter(aa);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View view, int index, long arg3) {
                new AsyncTask<Integer, Void, BluetoothSocket>() {
                    @Override
                    protected BluetoothSocket doInBackground(Integer... params) {
                        BluetoothDevice device = foundDevices.get(params[0]);
                        BluetoothSocket socket = null;
                        try {
                            socket = device.createRfcommSocketToServiceRecord(uuid);

                            Log.i("", "Device Name: " + device.getName());

                            if (mBluetoothAdapter.isDiscovering()) {
                                Log.w("", "Socket is already discovering");
                                mBluetoothAdapter.cancelDiscovery();
                            }

                            if (socket.isConnected()) {
                                Log.w("", "Socket is already connected");
                            } else {
                                socket.connect();
                            }
                            return socket;
                        } catch (IOException e) {
                            Log.e("", "" + e);
                            if (socket != null) {
                                try {
                                    socket.close();
                                } catch (IOException e1) {
                                    Log.e("", "" + e);
                                }
                            }
                        }

                        return null;
                    }

                    @Override
                    protected void onPostExecute(BluetoothSocket socket) {
                        if (socket == null) {
                            Log.e("", "Socket is NULL");
                            exit();
                        } else {
                            switchUI(socket);
                        }
                    }
                }.execute(index);
            }
        });
    }


    private void switchUI(BluetoothSocket socket) {
        final TextView messageText = (TextView) findViewById(R.id.text_messages);
        messageText.setVisibility(View.VISIBLE);
        final EditText textEntry = (EditText) findViewById(R.id.text_message);
        textEntry.setVisibility(View.VISIBLE);
        textEntry.setEnabled(true);
        list.setVisibility(View.GONE);

        final Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(textEntry.getText().toString());
                textEntry.setText("");
            }
        });

        mSocketListener = new BluetoothSocketListener(socket, messageText);
        mMessageListener = new Thread(mSocketListener);
        mMessageListener.start();
    }


    private void sendMessage(String msg) {
        try {
            if (mSocket != null && mSocket.isConnected()) {
                OutputStream outStream = mSocket.getOutputStream();
                if (outStream != null) {
                    byte[] byteString = (msg + " ").getBytes();
                    byteString[byteString.length - 1] = 0;
                    outStream.write(byteString);
                }
            }
        }
        catch (IOException e) { Log.e("", "" + e); }
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 200) {
                lastUpdate = curTime;
                long diffTime = curTime - lastUpdate;

                float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    sendMessage("accelerometer:" + x + ":" + y + ":" + z + ":" + System.currentTimeMillis());
                }

                lastX = x;
                lastY = y;
                lastZ = z;
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

} // class YeloserClient
