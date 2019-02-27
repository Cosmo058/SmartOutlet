package com.raquelrv.smartoutlet;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.UUID;

public class Outlet extends AppCompatActivity {
    private static final String TAG = "Outlet";
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mBluetoothConnection = new BluetoothConnectionService(Outlet.this);

        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        mBTDevice = intent.getParcelableExtra(MainActivity.BLUETOOTH_DEVICE);


        mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);

        Log.d(TAG, message);

        GraphView graph = (GraphView) findViewById(R.id.graph);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[] {
                new DataPoint(0, 1),
                new DataPoint(1, 5),
                new DataPoint(2, 3),
                new DataPoint(3, 2),
                new DataPoint(4, 6)
        });
        graph.addSeries(series);
    }
}
