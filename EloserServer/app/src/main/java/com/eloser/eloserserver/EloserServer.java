package com.eloser.eloserserver;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.eloser.eloserserver.util.Log;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class EloserServer extends Activity {
    private final static int DISCOVERY_REQUEST = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private Thread mMessageListener = null;
    InputStream mInStream = null;

    private volatile boolean mGameThreadRunning = true;

    private UUID uuid = UUID.fromString("00E105EA-0000-1000-8000-00805F9B34FB");

    // INNER CLASSES

    private class MessagePoster implements Runnable {
        private String message;

        public MessagePoster(String message) {
            this.message = message;
        }

        public void run() {
            TextView textView = (TextView)findViewById(R.id.info0);
            textView.setText(message);

            if (message.contains("accelerometer")) {
                String[] tokens = message.split(":");
                float aX = Float.parseFloat(tokens[1]);
                final GameView gameView = (GameView)findViewById(R.id.play_zone);
                gameView.setAcelerometerData(aX);
            }
            else if (message.contains("stop")) {
                Log.i("", "Game is stopped");
            }
        }
    }


    private Thread mGameThread = new Thread() {
        @Override
        public void run() {
            super.run();
            final GameView gameView = (GameView)findViewById(R.id.play_zone);
            while (mGameThreadRunning) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        gameView.invalidate();
                    }
                });

                try {
                    Thread.sleep(30);
                }
                catch (InterruptedException e) { Log.e("", "" + e); }
            }
        }
    };


    private class BluetoothSocketListener implements Runnable {
        private final static int BUFFER_SIZE = 1024;

        public BluetoothSocketListener(BluetoothSocket socket) {
            mSocket = socket;
        }

        public void run() {
            mGameThread.start();
            byte[] buffer = new byte[BUFFER_SIZE];
            try {
                mInStream = mSocket.getInputStream();
                StringBuilder message = new StringBuilder();
                while (true) {
                    message.setLength(0);
                    int bytesRead = mInStream.read(buffer);
                    if (bytesRead != -1) {
                        while ((bytesRead == BUFFER_SIZE) && (buffer[BUFFER_SIZE - 1] != 0)) {
                            message.append(new String(buffer, 0, bytesRead));
                            bytesRead = mInStream.read(buffer);
                        }
                        message.append(new String(buffer, 0, bytesRead - 1));
                        new Handler(Looper.getMainLooper()).post(new MessagePoster(message.toString()));

                        if (message.toString().equals("stop")) break;
                    }
                }
            }
            catch (IOException e) { Log.e("", "" + e); }

            Log.d("", "Closing listener: " + Thread.currentThread().getName());

            mGameThreadRunning = false;
            mGameThread.interrupt();
            try {
                mGameThread.join();
            } catch (InterruptedException e) {
                Log.e("", "" + e);
            }

            exit();
        }
    }

    private BluetoothSocketListener mSocketListener = null;

    // METHODS

    private void sendMessage(BluetoothSocket socket, String msg) {
        Log.d("*****", "msg = " + msg);
        try {
            OutputStream outStream = socket.getOutputStream();
            byte[] byteString = (msg + " ").getBytes();
            byteString[byteString.length - 1] = 0;
            outStream.write(byteString);
        }
        catch (IOException e) { Log.e("", "" + e); }
    }


    private void exit() {
        try {
            if (mInStream != null) {
                mInStream.close();
            }

            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            Log.e("", "" + e);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eloser_server);

        final GameView gameView = (GameView)findViewById(R.id.play_zone);
        gameView.init();

        int[] infoIds = { R.id.info0, R.id.info1, R.id.info2, R.id.info3, R.id.info4 };

        Log.initLog(this, infoIds, Log.LogLevel.Verbose);

        configureBluetooth();
        startListener();
    }


    private void configureBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    private void startListener() {
        Intent disc = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(disc, DISCOVERY_REQUEST);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_eloser_server, menu);
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DISCOVERY_REQUEST) {
            boolean isDiscoverable = resultCode > 0;
            if (isDiscoverable) {
                String name = "eloserserver";
                try {
                    final BluetoothServerSocket btServer =
                            mBluetoothAdapter.listenUsingRfcommWithServiceRecord(name, uuid);
                    new AsyncTask<Integer, Void, BluetoothSocket>() {
                        @Override
                        protected BluetoothSocket doInBackground(Integer... params) {
                            try {
                                BluetoothSocket socket = btServer.accept(params[0] * 1000);
                                btServer.close();
                                return socket;
                            } catch (IOException e) { Log.e("", "" + e); }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(BluetoothSocket socket) {
                            switchUI(socket);
                        }
                    }.execute(resultCode);
                } catch (IOException e) { Log.e("", "" + e); }
            }
        }
    }


    @Override
    protected void onDestroy() {
        Log.i("*****", "Begin");

        super.onDestroy();

        Log.i("*****", "End");
    }


    private void switchUI(final BluetoothSocket socket) {
        if (socket == null) {
            Log.e("", "Socket is NULL");
            return;
        }

        final EditText textEntry = (EditText) findViewById(R.id.text_message);
        textEntry.setEnabled(true);

        final Button send = (Button) findViewById(R.id.send);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(socket, textEntry.getText().toString());
                textEntry.setText("");
            }
        });

        final Button stop = (Button) findViewById(R.id.exit_button);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage(socket, "stop");
                textEntry.setText("");
            }
        });

        mSocketListener = new BluetoothSocketListener(socket);
        mMessageListener = new Thread(mSocketListener);
        mMessageListener.start();
    }

} // class EloserServer
