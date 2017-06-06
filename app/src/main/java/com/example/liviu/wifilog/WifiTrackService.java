package com.example.liviu.wifilog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;

import java.util.Date;
import java.util.List;


public class WifiTrackService extends Service{
    private static final String ACTION_TRACK_NETWORK = "com.example.liviu.wifilog.action.TRACK";
    private static final String PARAM_NETWORK_NAME = "com.example.liviu.wifilog.extra.NETWORK";

    private Thread mWorker;
    private volatile boolean mWorkerRunning = false;
    private String mCurrentNetwork;
    private Date mStartTime;
    private boolean mNameChange = false;
    private boolean mOutOfReach = false;

    private BroadcastReceiver mWifiReceiver;

    {
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                List<ScanResult> list = wifiManager.getScanResults();

                synchronized (this) {
                    mOutOfReach = !list.contains(mCurrentNetwork);
                    notify();
                }

            }
        };
    }

    public WifiTrackService() {super();}

    public static void startTracking(Context context, String network) {
        Intent intent = new Intent(context, WifiTrackService.class);
        intent.setAction(ACTION_TRACK_NETWORK);
        intent.putExtra(PARAM_NETWORK_NAME, network);
        context.startService(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        registerReceiver(mWifiReceiver, filters);
    }

    @Override
    public void onDestroy() {
        mWorkerRunning = false;
    }

    @Override
    public int onStartCommand (Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_TRACK_NETWORK.equals(action)) {
                /* changing the name will change the tracked network in the worker thread */
                mCurrentNetwork = intent.getStringExtra(PARAM_NETWORK_NAME);

                if (mWorkerRunning == false) {
                    mWorker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleActionStartTracking(mCurrentNetwork);
                        }
                    });

                    mWorker.start();
                } else {
                    synchronized (this) {
                        mNameChange = true;
                        notify();
                    }
                }
            }
        }
        return START_STICKY;
    }

    private void handleActionStartTracking(String network) {
        mWorkerRunning = true;

        while(mWorkerRunning) {
            mStartTime = new Date();
            synchronized (this) {
                while(!mNameChange && !mOutOfReach) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        /* do nothing */
                    }
                }
            }

            Date crtTime = new Date();

            //TODO - record time difference
        }
    }


}
