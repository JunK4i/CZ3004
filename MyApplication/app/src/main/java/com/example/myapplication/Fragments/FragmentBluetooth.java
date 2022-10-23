package com.example.myapplication.Fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.ChatUtils;
import com.example.myapplication.Entities.AppViewModel;
import com.example.myapplication.Entities.ArenaGame1;
import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * This fragment contains all the required functions for bluetooth connection and communication
 */
public class FragmentBluetooth extends Fragment {
    private AppViewModel viewModel;
    private ArenaGame1 updatedGame1;

    private BluetoothAdapter bluetoothAdapter;

    private ProgressBar progressScanDevices;
    private ListView listPairedDevices, listAvailableDevices;
    private ArrayAdapter<String> adapterPairedDevices, adapterAvailableDevices;
    private List<BluetoothDevice> tmpBtChecker = new ArrayList<BluetoothDevice>();

    private final int LOCATION_PERMISSION_REQUEST = 101;
    private final int BLUETOOTH_SCAN_REQUEST = 102;
    private final int BLUETOOTH_CONNECT_REQUEST = 103;

    public static final int MESSAGE_STATE_CHANGED = 0;
    public static final int MESSAGE_READ = 1;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_DEVICE_NAME = 3;
    public static final int MESSAGE_TOAST = 4;
    public static final String DEVICE_NAME = "deviceName";
    public static final String TOAST = "toast";
    private String connectedDevice;

    private ListView listReadChat, listWriteChat, status;
    private EditText edCreateMessage;
    private Button btnSendMessage, onBtn, offBtn, discoverableBtn, searchBtn, resetButton;

    // Inflate the layout for this fragment
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Display Views
        listWriteChat = view.findViewById(R.id.list_conversation);
        listReadChat = view.findViewById(R.id.list_conversation2);
        status = view.findViewById(R.id.status);
        progressScanDevices = view.findViewById(R.id.progress_scan_devices);
        listPairedDevices = view.findViewById(R.id.list_paired_devices);
        listAvailableDevices = view.findViewById(R.id.list_available_devices);
        edCreateMessage = view.findViewById(R.id.ed_enter_message);

        // Init view model
        viewModel = new ViewModelProvider(requireActivity()).get(AppViewModel.class);
        updatedGame1 = viewModel.getMutableLiveGame1().getValue(); // get value at this instance
        if(!updatedGame1.isChatUtilsInit()){ // initialise the chatUtil if not yet init
            updatedGame1.initChatUtils(handler);
            viewModel.getMutableLiveGame1().setValue(updatedGame1); // update game after init
        }

        // Listen and update
        viewModel.getMutableLiveGame1().observe(getViewLifecycleOwner(), liveGame1Instance -> {
            updatedGame1 = liveGame1Instance;
        });

        // User input views
        btnSendMessage = view.findViewById(R.id.btn_send_msg);
        onBtn = view.findViewById(R.id.menu_enable_bluetooth);
        offBtn = view.findViewById(R.id.menu_disable_bluetooth);
        discoverableBtn = view.findViewById(R.id.menu_discoverable_bluetooth);
        searchBtn = view.findViewById((R.id.menu_search_devices));
        resetButton = view.findViewById(R.id.resetButton);

        // Adapter to dynamically funnel data to views
        adapterPairedDevices = new ArrayAdapter<String>(getActivity(), R.layout.device_list_item);
        adapterAvailableDevices = new ArrayAdapter<String>(getActivity(), R.layout.device_list_item);
        listPairedDevices.setAdapter(adapterPairedDevices);
        listAvailableDevices.setAdapter(adapterAvailableDevices);
        listReadChat.setAdapter(updatedGame1.getReadAdapter());
        listWriteChat.setAdapter(updatedGame1.getWriteAdapter());
        status.setAdapter(updatedGame1.getStatusAdapter());

        // Adapter link between physical connection and code
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getActivity(), "Bluetooth not supported", Toast.LENGTH_SHORT).show();
        }

        // Register broadcast listener in activity context to listen for found and
        // discovery finished
        IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND); // type of intent
        getActivity().registerReceiver(bluetoothDeviceListener, intentFilter);
        IntentFilter intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(bluetoothDeviceListener, intentFilter1);

        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = edCreateMessage.getText().toString() + "#";
                if (!message.isEmpty()) {
                    edCreateMessage.setText("");
                    updatedGame1.getChatUtils().write(message.getBytes());
                }
            }
        });
        onBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("onBtn", "click");
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_CONNECT }, BLUETOOTH_CONNECT_REQUEST);
                    Log.e("onBtn", "askPermission");
                    return;
                }
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable(); // Last Resort. This enables the device bluetooth without user
                                               // permission. API 33 onwards deprecated. Intended only for "power
                                               // manager" apps.
                    Toast.makeText(getActivity(), "Bluetooth turned on", Toast.LENGTH_SHORT).show();
                    Log.e("onBtn", "btenabled");
                } else {
                    Toast.makeText(getActivity(), "Bluetooth is already on", Toast.LENGTH_SHORT).show();
                    Log.e("onBtn", "btalrenabled");
                }

            }
        });
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("offBtn", "click");
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_CONNECT }, BLUETOOTH_CONNECT_REQUEST);
                    Log.e("offBtn", "askPermission");
                    return;
                }
                if (bluetoothAdapter.isEnabled()) {
                    updatedGame1.getChatUtils().stop();
                    bluetoothAdapter.disable();
                    Toast.makeText(getActivity(), "Bluetooth turned off", Toast.LENGTH_SHORT).show();
                    Log.e("offBtn", "btdisabled");
                } else {
                    Toast.makeText(getActivity(), "Bluetooth is already off", Toast.LENGTH_SHORT).show();
                    Log.e("offBtn", "btalrdisabled");
                }
            }
        });
        discoverableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("discoverableBtn", "click");
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_SCAN }, BLUETOOTH_SCAN_REQUEST);
                    Log.e("discoverableBtn", "askPermission");
                    return;
                }
                if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                    startActivity(discoveryIntent);
                    Log.e("discoverableBtn", "discoverability enabled");
                } else {
                    Toast.makeText(getActivity(), "Device already discoverable", Toast.LENGTH_SHORT).show();
                    Log.e("discoverableBtn", "already discoverable");
                }
            }
        });
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("searchBtn", "click");
                if (ContextCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, LOCATION_PERMISSION_REQUEST);
                    Log.e("searchBtn", "askPermission");
                    return;
                }
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    Log.e("scan devices", "0");
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_SCAN }, BLUETOOTH_SCAN_REQUEST);
                    return;
                }

                progressScanDevices.setVisibility(View.VISIBLE);
                adapterAvailableDevices.clear();

                if (bluetoothAdapter == null) {
                    Log.e("scan devices", "no btadapter");
                }

                if (bluetoothAdapter.isDiscovering()) { // cancel current search
                    bluetoothAdapter.cancelDiscovery();
                }

                Toast.makeText(getActivity(), "Scan started", Toast.LENGTH_SHORT).show();
                bluetoothAdapter.startDiscovery();
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updatedGame1.getReadAdapter().clear();
            }
        });

        // Display avail and paired devices. Onclick to connect using chatUtils
        listAvailableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (bluetoothAdapter.isEnabled()) {
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);
                    Log.d("on click available", address);
                    updatedGame1.getChatUtils().connect(bluetoothAdapter.getRemoteDevice(address));
                } else {
                    Toast.makeText(getActivity(), "Please turn BT on", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        Log.e("onCreatedView", "getBondedDevices");
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                adapterPairedDevices.add(device.getName() + "\n" + device.getAddress());
                Log.d("paired devices", device.getName());
            }
        }

        listPairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_SCAN }, BLUETOOTH_SCAN_REQUEST);
                    return;
                }
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.cancelDiscovery();
                    String info = ((TextView) view).getText().toString();
                    String address = info.substring(info.length() - 17);
                    Log.d("Address", address);
                    updatedGame1.getChatUtils().connect(bluetoothAdapter.getRemoteDevice(address));
                } else {
                    Toast.makeText(getActivity(), "Please turn BT on", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Helper function to setState for bt status
    private final BroadcastReceiver bluetoothDeviceListener = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d("activity status", "get action");
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                tmpBtChecker.clear();
            }
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if (ActivityCompat.checkSelfPermission(getActivity(),
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[] { Manifest.permission.BLUETOOTH_CONNECT }, BLUETOOTH_CONNECT_REQUEST);
                    return;
                }
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED && !tmpBtChecker.contains(device)) {
                    tmpBtChecker.add(device);
                    if (device.getName() != null) {
                        adapterAvailableDevices.add(device.getName() + "\n" + device.getAddress());
                        Log.d("Device Listener", "discover" + device.getName());
                    }

                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                progressScanDevices.setVisibility(View.GONE);
                if (adapterAvailableDevices.getCount() == 0) {
                    Toast.makeText(context, "No new devices found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Click on device to start pairing", Toast.LENGTH_SHORT).show();

                }
            }
        }
    };

    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (result) {
                        Log.d("activity status", "Permission given");
                    }
                }
            });

    // Callback handler passed into chat utils
    // Chat utils will call the handler to update the display of status, input and output
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case MESSAGE_STATE_CHANGED:
                    switch (message.arg1) {
                        case ChatUtils.STATE_NONE:
                            updatedGame1.setBtStatus("  Not Connected");
                            viewModel.getMutableLiveGame1().setValue(updatedGame1);
                            break;
                        case ChatUtils.STATE_LISTEN:
                            updatedGame1.setBtStatus("  Listening");
                            viewModel.getMutableLiveGame1().setValue(updatedGame1);
                            break;
                        case ChatUtils.STATE_CONNECTING:
                            updatedGame1.setBtStatus("  Connecting");
                            viewModel.getMutableLiveGame1().setValue(updatedGame1);
                            break;
                        case ChatUtils.STATE_CONNECTED:
                            updatedGame1.setBtStatus("  Connected: " + connectedDevice);
                            viewModel.getMutableLiveGame1().setValue(updatedGame1);
                            break;
                    }
                    break;
                case MESSAGE_READ:
                    byte[] buffer = (byte[]) message.obj;
                    String inputBuffer = new String(buffer, 0, message.arg1);
                    Log.e("readFromBt", "before" + inputBuffer);
                    inputBuffer = inputBuffer.replaceAll("\n", "");
                    Log.e("readFromBt", "after" + inputBuffer);
                    String temp_read = updatedGame1.getTempReadMsg(); // this get the leftover from prev
                    temp_read = temp_read.concat(inputBuffer); // add it to the new msg
                    Pattern pound = Pattern.compile("#");
                    String[] inputs = pound.split(temp_read); // separate by pound
                    if (inputs.length != 0) {
                        if(!temp_read.equals("")) {
                            if (temp_read.charAt(temp_read.length() - 1) == '#') {
                                updatedGame1.addReadMessage(inputs[0]);
                                for (int i = 1; i < inputs.length; i++) {
                                    updatedGame1.addReadMessage(inputs[i]);
                                }
                                updatedGame1.setTempReadMsg("");
                            } else {
                                if(!(inputs.length == 1)){
                                    updatedGame1.addReadMessage(inputs[0]);
                                }
                                for (int i = 1; i < inputs.length - 1; i++) {
                                    updatedGame1.addReadMessage(inputs[i]);
                                }
                                updatedGame1.setTempReadMsg(inputs[inputs.length - 1]);
                            }
                        }
                    }
                    viewModel.getMutableLiveGame1().setValue(updatedGame1);
                    break;
                case MESSAGE_WRITE:
                    byte[] buffer1 = (byte[]) message.obj;
                    String outputBuffer = new String(buffer1);
                    updatedGame1.addWriteMessage(outputBuffer);
                    viewModel.getMutableLiveGame1().setValue(updatedGame1);
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDevice = message.getData().getString(DEVICE_NAME);
                    try {
                        Toast.makeText(getActivity(), connectedDevice, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("error", "" + e);
                    }
                    break;
                case MESSAGE_TOAST:
                    try {
                        Toast.makeText(getActivity(), message.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("error", "" + e);
                    }
                    break;
            }
            return false;
        }
    });

}