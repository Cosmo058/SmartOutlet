package com.raquelrv.smartoutlet;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG  = "MainActivity";

    BluetoothAdapter mBluetoothAdapter;
    Button btnEnableDisable_Discoverable;

    BluetoothConnectionService mBluetoothConnection;

    Button btnStartConnection;
    Button btnSend;

    EditText etSend;

    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String EXTRA_MESSAGE = "com.raquelrv.smartoutlet.MESSAGE";
    public static final String BLUETOOTH_DEVICE = "com.raquelrv.smartoutlet.BLUETOOTH_DEVICE";
    public static final String MY_UUID = "com.raquelrv.smartoutlet.MY_UUID";

    BluetoothDevice mBTDevice;

    public ArrayList<BluetoothDevice> mBTDevices;

    public DeviceListAdapter mDeviceListAdapter;

    ListView lvNewDevices;

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state =  intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch (state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "onReceive: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                final int mode =  intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode){
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability enabled. Able to receive connections");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disable. Not able to receive connections");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected");
                        break;
                }
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG,"onReceive: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG,"onReceive: "+device.getName()+": "+device.getAddress());

                mDeviceListAdapter = new DeviceListAdapter(context,R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDED");
                    mBTDevice = mDevice;
                }

                //case2: creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BroadcastReceiver: BOND_BONDING");
                }

                //case3: breaking a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BroadcastReceiver: BOND_NONE");
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy: called");
        super.onDestroy();
        try{
            unregisterReceiver(mBroadcastReceiver1);
            unregisterReceiver(mBroadcastReceiver2);
            unregisterReceiver(mBroadcastReceiver3);
            unregisterReceiver(mBroadcastReceiver4);
        }catch (Exception e){
            Log.d(TAG,"One or more BrodcastReceiver were unregistered");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnONOFF = findViewById(R.id.btnONOFF);
        btnEnableDisable_Discoverable = findViewById(R.id.btnDiscoverable_on_off);
        lvNewDevices = findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        btnStartConnection = findViewById(R.id.btnStartConnection);
        btnSend = findViewById(R.id.btnSend);
        etSend = findViewById(R.id.editText);

        //Broadcast when bond state changes (ie: pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4,filter);

        lvNewDevices.setOnItemClickListener(MainActivity.this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btnONOFF.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Log.d(TAG,"onClick: enabling/disabling bluetooth");
                enableDisableBT();
            }
        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConnection();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textToSend = etSend.getText().toString();
                textToSend += "\n";

                byte[] bytes = textToSend.getBytes(Charset.defaultCharset());
                mBluetoothConnection.write(bytes);
            }
        });
    }

    public void startConnection(){
        startBTConnection(mBTDevice, MY_UUID_INSECURE);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG,"startBTConnection: Init Bluetooth connection...");
        mBluetoothConnection.startClient(device,uuid);
    }

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: enabling BT.");
            Intent enableBTIntent =  new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent =  new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }

        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent =  new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    public void btnEnableDisable_Discoverable(View view){
        Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300s.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2,intentFilter);
    }

    public void btnDiscover(View view){
        mBTDevices.clear();
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Canceling discovery.");

            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
        }
        if(!mBluetoothAdapter.isDiscovering()){
            checkBTPermissions();

            mBluetoothAdapter.startDiscovery();
            IntentFilter discoveryDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoveryDevicesIntent);
        }
    }

    @TargetApi(23)
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

            if(permissionCheck != 0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }else{
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP");
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int devices_count = parent.getAdapter().getCount();
        for(int i = 0; i<devices_count; i++){
            ViewGroup list_element = (ViewGroup)view.getParent();
            list_element.getChildAt(i).setBackgroundColor(Color.WHITE);
        }

        view.setBackgroundColor(Color.argb(32,0,133,119));

        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG,"onItemClick: You clicked on a device");
        String deviceName = mBTDevices.get(position).getName();
        String deviceAddress = mBTDevices.get(position).getAddress();

        Log.d(TAG,"onItemClick: deviceName = " + deviceName);
        Log.d(TAG,"onItemClick: deviceAddress = "+ deviceAddress);


        //create the bond
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG,"Trying to pair with " + deviceName);
            mBTDevices.get(position).createBond();

            mBTDevice = mBTDevices.get(position);
            mBluetoothConnection = new BluetoothConnectionService(MainActivity.this);
        }
    }

    public void NewActivity(View view) {
        Intent intent = new Intent(this, Outlet.class);
        String message = "Test";
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(BLUETOOTH_DEVICE,mBTDevice);
        intent.putExtra(MY_UUID,MY_UUID_INSECURE);

        startActivity(intent);
    }
}
