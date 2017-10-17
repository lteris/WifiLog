package com.example.liviu.wifilog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import java.util.Date;
import java.util.List;


public class WifiTrackService extends Service{
    private static final String ACTION_TRACK_NETWORK = "com.example.liviu.wifilog.action.TRACK";
    private static final String PARAM_NETWORK_NAME = "com.example.liviu.wifilog.extra.NETWORK";
    private static final String ACTION_PAUSE_TRACKING = "com.example.liviu.wifilog.action.PAUSE";

    private Thread mWorker;
    private volatile boolean mWorkerRunning = false;
    private String mCurrentNetwork;
    private Date mStartTime;
    private boolean mNameChange = false;
    private boolean mOutOfReach = false;
    private WifiTrackingBinder mBinder = new WifiTrackingBinder();

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

    public class WifiTrackingBinder extends Binder {
        public WifiTrackService getServiceInstance() {
            return WifiTrackService.this;
        }
    }

    public WifiTrackService() {super();}

    /**
     * Called from WifiTrackFragment - starts tracking the network "network".
     * @param context
     * @param network
     */
    public static void startTracking(Context context, String network) {
        Intent intent = new Intent(context, WifiTrackService.class);
        intent.setAction(ACTION_TRACK_NETWORK);
        intent.putExtra(PARAM_NETWORK_NAME, network);
        context.startService(intent);

        //TODO call startForeground ( not here, in service)
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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

    /**
     * Start the service - triggered from this.startTracking.
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
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
                            handleActionDoTracking(mCurrentNetwork);
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

    /**
     * Worker thread - waits for changes in the currently tracked network and does back-ups of
     * the running time to the content provider.
     * @param network
     */
    private void handleActionDoTracking(String network) {
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

            long diffTime = crtTime.getTime() - mStartTime.getTime();

            getContentResolver().insert(WifiTimeProvider.TIMES_URI,
                    WifiTimeProvider.newTimesEntry(mStartTime.getTime(), diffTime));
        }
    }


}
