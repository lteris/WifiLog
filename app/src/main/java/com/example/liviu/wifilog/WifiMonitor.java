package com.example.liviu.wifilog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.net.wifi.*;

import java.util.ArrayList;
import java.util.List;

public class WifiMonitor extends AppCompatActivity {

    private ListView wifiListView;
    private TextView wifiTextView;
    private ArrayList<String> wifiNamesList;
    private ArrayAdapter<String> wifiListAdapter;

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            List<ScanResult> list = wifiManager.getScanResults();

            for (ScanResult r: list) {
                wifiNamesList.add(r.BSSID.toString());
            }

            wifiListAdapter.notifyDataSetChanged();
        }
    };


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

        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(wifiReceiver, filters);
    }

    protected void onDestroy() {
        if (wifiReceiver != null) {
            unregisterReceiver(wifiReceiver);
            wifiReceiver = null;
        }
        super.onDestroy();
    }

    public void setNetwork(View view) {
        Log.d("DBG", "Clicked button");


    }
}
