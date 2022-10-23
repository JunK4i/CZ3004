package com.example.myapplication;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.example.myapplication.Fragments.FragmentBluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * ChatUtils handles the threads associated with bluetooth connection.
 * It is initialised with a handler callback function.
 *
 * Handler allows you to send and process Message and Runnable objects associated with a thread's MessageQueue
 * Synchronised keyword avoids two or more threads updated mutable shared data at the same time by allowing only one thread to execute at once.
 *
 * Flow:
 * 1. new ChatUtils() - Create instance
 * 2. ChatUtills.connect(device) - Create connect thread, state is connecting. Call ConnectThread.start(), creating a new thread which will call .run() when the it gets a chance to execute
 * 3. ConnectThread.run() -  Connect BluetoothSocket to device and call connected(socket, device)
 * 4. ChatUtils.connected(socket, device) - Kill connect thread, create connectedThread. Call ConnectedThread.start(). connectedThread.run() will begin listening for messages.
 *
 * Alt Flow:
 * onfail - send failure message back through handler and clear all threads with ChatUtils.start()/ChatUtils.stop()
 */
public class ChatUtils {

    private final Handler handler;
    private ConnectThread connectThread;
    private AcceptThread acceptThread;
    private BluetoothAdapter bluetoothAdapter;
    private ConnectedThread connectedThread;
    private BluetoothDevice lastDevice;
    private boolean reconnect = false;

    private final UUID APP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final String APP_NAME = "BT3";
    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    private final int BLUETOOTH_SCAN_REQUEST = 102;
    private final int BLUETOOTH_CONNECT_REQUEST = 103;

    private int state;

    // Constructor will get, handler, state and bluetooth adapter.
    public ChatUtils(Handler handler){
        this.handler = handler;

        state = STATE_NONE;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public int getState(){
        return state;
    }

    // Send state to handler
    public synchronized void setState(int state){
        this.state = state;
        handler.obtainMessage(FragmentBluetooth.MESSAGE_STATE_CHANGED, state, -1).sendToTarget();
    }

    // ChatUtils.start() cancels existing threads, starts accept thread and state is listening. (should be none?)
    private synchronized void start(){
        Log.e("ChatUtils.start", "start");
        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
            Log.e("ChatUtils.start", "delete connect thread");
        }
        if (acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
            Log.e("ChatUtils.start", "start accept thread");
        }
        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
            Log.e("ChatUtils.start", "delete connected thread");
        }

        setState(STATE_LISTEN);
        Log.e("ChatUtils.start", "set state");
    }

    // ChatUtils.stop() cancels all existing threads, and state is none.
    public synchronized void stop(){
        Log.e("ChatUtils.stop", "start");
        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
            Log.e("ChatUtils.stop", "delete connect thread");
        }

        if (acceptThread != null){
            acceptThread.cancel();
            acceptThread = null;
            Log.e("ChatUtils.stop", "delete accept thread");
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
            Log.e("ChatUtils.stop", "delete connected thread");
        }
        reconnect = false;
        setState(STATE_NONE);
        Log.e("ChatUtils.stop", "set state");
    }

    // Kill existing connectThread, connectedThread
    // Initialise a connect thread
    public void connect(BluetoothDevice device){
        Log.e("ChatUtils.connect", "start " + device);
        if (state == STATE_CONNECTING){
            connectThread.cancel();
            connectThread = null;
            Log.e("ChatUtils.connect", "delete thread");
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
            Log.e("ChatUtils.connect", "delete connected thread");
        }
//TODO
//        if (acceptThread != null){
//            acceptThread.cancel();
//            acceptThread = null;
//            Log.e("ChatUtils.connect", "delete accept thread");
//        }

        connectThread = new ConnectThread(device);
        connectThread.start();
        Log.e("ChatUtils.connect", "create/start new connect thread");

        setState(STATE_CONNECTING);
        Log.e("ChatUtils.connect", "set state");
    }

    // Kill existing connectThread, connectedThread
    // Start connection between socket and device. Initialise connectedThread
    @SuppressLint("MissingPermission")
    private synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.e("ChatUtils.connected", "start");
        if (connectThread != null){
            connectThread.cancel();
            connectThread = null;
            Log.e("ChatUtils.connected", "delete connect thread");
        }

        if (connectedThread != null) {
            connectedThread.cancel();
            connectedThread = null;
            Log.e("ChatUtils.connected", "delete connected thread");
        }

        //TODO
        if (acceptThread != null){
            acceptThread = null;
            Log.e("ChatUtils.connect", "delete accept thread");
        }

        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        Log.e("ChatUtils.connected", "create/start new connected thread");

        lastDevice = device;

        Message message = handler.obtainMessage(FragmentBluetooth.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBluetooth.DEVICE_NAME, device.getName());
        message.setData(bundle);
        handler.sendMessage(message);

        setState(STATE_CONNECTED);
        Log.e("ChatUtils.connected", "set state");
    }

    // Write to the connectedThread
    public void write(byte[] buffer) {
        synchronized (this) {
            if (state != STATE_CONNECTED) {
                Log.e("write","not connected");
                return;
            }
            Log.e("write","connected, msg: "+ buffer.toString());
            connectedThread.write(buffer);
        }
    }

    // Retrieve incoming connections
    private class AcceptThread extends Thread{
        private BluetoothServerSocket serverSocket;

        @SuppressLint("MissingPermission")
        public AcceptThread(){
            Log.e("ChatUtils.AcceptThread", "Constructor");
            BluetoothServerSocket tmp = null;
            try{
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, APP_UUID); //Create a listening, secure RFCOMM Bluetooth socket with Service Record.
            } catch (IOException e) {
                Log.e("AcceptThread->Constructor", e.toString());
            }
            serverSocket = tmp;
        }

        public void run(){
            Log.e("ChatUtils.AcceptThread", "Run");
            BluetoothSocket socket = null;
            while(true){
                try {
//                    if (reconnect){
//                        //TODO
//                        Log.e("ChatUtils.AcceptThread", "Connect last");
//                        connect(lastDevice);
//                        reconnect = false;
//
//                    } else {
                        Log.e("ChatUtils.AcceptThread", "Accept socket1");
                        socket = serverSocket.accept();
                        Log.e("ChatUtils.AcceptThread", "Accept socket2");

                } catch (IOException e) {
                    Log.e("Accept->Run", e.toString());
                    break;
//                    try {
//                        serverSocket.close();
//                    } catch (IOException e1) {
//                        Log.e("Accept->Close", e1.toString());
//                    }
                }

                if (socket != null) {
                    switch (state) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            //TODO
                            if (reconnect){
                                Log.e("ChatUtils.AcceptThread", "Connect last2");
                                connected(socket, lastDevice);
                            } else {
                                Log.e("ChatUtils.AcceptThread", "Connect another");
                                connected(socket, socket.getRemoteDevice());
                            }
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {
                                socket.close();
                            } catch (IOException e) {
                                Log.e("Accept->CloseSocket", e.toString());
                            }
                            break;
                    }
                }
            }
        }

        public void cancel(){
            try{
                serverSocket.close();

            } catch (IOException e) {
                Log.e("Accept->CloseServer", e.toString());
            }
        }
    }

    // Initiate outgoing connection
    private class ConnectThread extends Thread{
        private final BluetoothSocket socket;
        private final BluetoothDevice device;

        // Initialise the socket and the device
        @SuppressLint("MissingPermission")
        public ConnectThread(BluetoothDevice device){
            Log.e("ChatUtils.ConnectThread", "Constructor");
            this.device = device;
            BluetoothSocket tmp = null;
            try{
                Log.e("ChatUtils.ConnectThread", "Constructor1");
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("Connect->Constructor", e.toString());
            }
            socket = tmp;
            Log.e("ChatUtils.ConnectThread", "Create socket");
            setState(STATE_CONNECTING);
            Log.e("ChatUtils.ConnectThread", "set state");
        }

        @SuppressLint("MissingPermission")
        public void run(){
            Log.e("ChatUtils.ConnectThread", "Run");
            bluetoothAdapter.cancelDiscovery();
            try{
                Log.e("ChatUtils.ConnectThread", "Connect socket");
                socket.connect();
            } catch (IOException e) {
                Log.e("Connect->Run", e.toString());
                try{
                    Log.e("Connect->Run", "Close socket");
                    socket.close();
                } catch (IOException e1){
                    Log.e("Connect->CloseSocket", e1.toString());
                }
                connectionFailed();
                Log.e("ChatUtils.ConnectThread", "Connection failed");
                return;
            }

            // socket.connect() is successful
            synchronized (ChatUtils.this){ // remove existing connectThread
                connectThread = null;
            }
            connected(socket, device); // initialise connectedThread
        }

        public void cancel(){
            Log.e("ChatUtils.ConnectThread", "Cancel");
            try{
                Log.e("ChatUtils.ConnectThread", "Close socket");
                socket.close();
            } catch (IOException e){
                Log.e("Connect->Cancel", e.toString());
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket socket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.e("ChatUtils.ConnectedThread", "Create socket");
            this.socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                Log.e("ChatUtils.ConnectedThread", "Get input stream");
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when creating input stream " + e.toString());
            }
            try{
                Log.e("ChatUtils.ConnectedThread", "Get output stream");
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when creating output stream " + e.toString());

            }
            inputStream = tmpIn;
            outputStream = tmpOut;
        }

        //Receive msg
        public void run() {
            Log.e("ChatUtils.ConnectedThread", "Run");
            byte[] buffer = new byte[1024];
            int bytes;
            while(true){
                try {
                    bytes = inputStream.read(buffer);
                    Log.e("ChatUtils.ConnectedThread", "input stream");
                    handler.obtainMessage(FragmentBluetooth.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    Log.e("input", e.toString());
                    connectionLost();
                    break;
                }
            }
        }

        //Send msg
        public void write(byte[] buffer) {
            Log.e("ChatUtils.ConnectedThread", "Run");
            try {
                outputStream.write(buffer);
                handler.obtainMessage(FragmentBluetooth.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            } catch (IOException e) {
                Log.e("output", e.toString());

            }
        }

        public void cancel() {
            Log.e("ChatUtils.ConnectedThread", "Cancel");
            try {
                socket.close();
            } catch (IOException e) {
                Log.e("ConnectedThread", "Error occurred when creating input stream " + e.toString());
            }
        }
    }

    private void connectionLost() {
        Log.e("ChatUtils.connectionLost", "Run");
        Message message = handler.obtainMessage(FragmentBluetooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBluetooth.TOAST, "Connection Lost");
        message.setData(bundle);
        handler.sendMessage(message);

        reconnect = true;
        ChatUtils.this.start();
    }

    private synchronized void connectionFailed(){
        Log.e("ChatUtils.connectionFailed", "Run");
        Message message = handler.obtainMessage(FragmentBluetooth.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(FragmentBluetooth.TOAST, "Cant connect to the device");
        message.setData(bundle);
        handler.sendMessage(message);

        ChatUtils.this.start();
    }

    @SuppressLint("MissingPermission")
    public String getDevice(){
        if(lastDevice!=null){
            return lastDevice.getName();
        }
        return "";
    }

}
