package com.example.liviu.wifilog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

/**
 * Created by liviu on 21.02.17.
 */

public class WifiBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);

        List<ScanResult> list = wifiManager.getScanResults();

        //TODO
    }
}
