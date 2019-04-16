package com.raquelrv.smartoutlet;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;
import java.util.UUID;

public class Outlet extends AppCompatActivity {
    private static final String TAG = "Outlet";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static int time_index = 0;

    BluetoothConnectionService mBluetoothConnection;
    BluetoothDevice mBTDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Connected...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //------------------------------------------------------------------------------------------
        GraphView graph = findViewById(R.id.graph);

        final LineGraphSeries<DataPoint> voltage = new LineGraphSeries<>();
        final LineGraphSeries<DataPoint> temperature = new LineGraphSeries<>();
        final LineGraphSeries<DataPoint> current = new LineGraphSeries<>();

        final TextView textViewVolt = findViewById(R.id.DataReceived);
        final TextView textViewTemp = findViewById(R.id.Temp);
        final TextView textViewCurr = findViewById(R.id.Curr);

        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time [1/10 s]");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Volts[V] / Temperature[°C]");

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(60);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(130);

        graph.getViewport().setScalable(true);

        graph.getGridLabelRenderer().setPadding(55);

        voltage.setDrawDataPoints(true);
        voltage.setDataPointsRadius(5);

        temperature.setDrawDataPoints(true);
        temperature.setDataPointsRadius(5);
        temperature.setColor(Color.RED);

        current.setDrawDataPoints(true);
        current.setDataPointsRadius(5);
        current.setColor(Color.GREEN);

        graph.addSeries(voltage);
        graph.addSeries(temperature);
        graph.addSeries(current);

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String data_stream = intent.getStringExtra(BluetoothConnectionService.EXTRA_DATA);
                        data_stream = data_stream.replace("@","");
                        data_stream = data_stream.replace("$","");

                        Log.d(TAG,"data_stream = "+data_stream);

                        String data[] = data_stream.split("\\|" );

                        Log.d(TAG,"data[] size = "+ data.length);

                        textViewVolt.setText("Voltage: " +data[0]+"V");
                        textViewTemp.setText("Temperature: "+ data[1] +"°C");
                        textViewCurr.setText("Current: "+data[2]+"A");
                        
                        voltage.appendData(new DataPoint(time_index,Double.parseDouble(data[0])),true,60);
                        temperature.appendData(new DataPoint(time_index,Double.parseDouble(data[1])),true,60);
                        current.appendData(new DataPoint(time_index++,Double.parseDouble(data[2])),true,60);

                    }
                }, new IntentFilter(BluetoothConnectionService.ACTION_DATA_BROADCAST)
        );

        mBluetoothConnection = new BluetoothConnectionService(Outlet.this);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        mBTDevice = intent.getParcelableExtra(MainActivity.BLUETOOTH_DEVICE);

        mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);

        Log.d(TAG, message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBluetoothConnection.stop();
    }
}
