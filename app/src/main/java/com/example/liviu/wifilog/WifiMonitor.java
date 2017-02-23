package com.example.liviu.wifilog;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class WifiMonitor extends AppCompatActivity {

    private ListView wifiListView;
    private TextView wifiTextView;
    private Thread scanNetworkThread;
    private ArrayList<String> wifiNamesList;
    private ArrayAdapter<String> wifiListAdapter;

    private WifiManager wifiManager = (WifiManager) getApplicationContext().
            getSystemService(getApplicationContext().WIFI_SERVICE);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_monitor);

        wifiListView = (ListView)findViewById(R.id.listWifiDetected);
        wifiTextView = (TextView)findViewById(R.id.editWifiName);
        wifiNamesList = new ArrayList<String>();
        wifiListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, wifiNamesList);
        wifiListView.setAdapter(wifiListAdapter);

        wifiListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parentView, View childView,
                                       int position, long id)
            {
                Log.d("CIUCH", "Clicked on " + ((TextView)childView).getText());
                wifiTextView.setText(((TextView)childView).getText());
            }
        });

        scanNetworkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                setListContent();
            }
        });

        scanNetworkThread.start();
    }

    public void setNetwork(View view) {
        Log.d("DBG", "Clicked button");


    }

    private void setListContent() {
        //TODO - get wifi list
        wifiNamesList.add("CIUCH");
        wifiNamesList.add("ciuciuciuiuch");

        wifiManager.startScan();

        wifiListAdapter.notifyDataSetChanged();
    }
}
