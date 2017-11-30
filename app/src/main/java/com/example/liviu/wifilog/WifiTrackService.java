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
    private String mCurrentNetwork = null;
    private boolean mCurrentInRange = false;
    private boolean mTrackingPaused = false;

    private WifiTrackingBinder mBinder = new WifiTrackingBinder();
    private WifiTrackingEvent mTrackingEvent = WifiTrackingEvent.NONE;

    private enum WifiTrackingEvent {
        NONE,
        NAME_CHANGE,
        OUT_OF_REACH,
        BACK_IN_RANGE,
        TOGGLE_PAUSE,
        CLEAR_COUNTER
    }

    private BroadcastReceiver mWifiReceiver;

    /* the receiver is installed in onCreate - make sure it is intantiated by that point */
    {
        mWifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                synchronized (WifiTrackService.this) {
                    if (mCurrentNetwork == null)
                        return;
                }

                WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
                List<ScanResult> list = wifiManager.getScanResults();

                /* signaling the outer class */
                synchronized (WifiTrackService.this) {
                    if (WifiListFragment.listHasNetwork(list, mCurrentNetwork)) {
                        if (!mCurrentInRange) {
                            mTrackingEvent = WifiTrackingEvent.BACK_IN_RANGE;
                            notify();
                        }
                    } else {
                        mTrackingEvent = WifiTrackingEvent.OUT_OF_REACH;
                        notify();
                    }
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

    /**
     * Called from WifiTrackFragment - pause/unpause recording
     */
    public void pauseRecording() {
        synchronized (this) {
            mTrackingEvent = WifiTrackingEvent.TOGGLE_PAUSE;
            notify();
        }
    }

    /**
     * Called from WifiTrackFragment - discard the current recording
     */
    public void clearRecording() {
        synchronized (this) {
            mTrackingEvent = WifiTrackingEvent.CLEAR_COUNTER;
            notify();
        }
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
                final String newNetwork = intent.getStringExtra(PARAM_NETWORK_NAME);

                if (mWorkerRunning == false) {
                    mWorker = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            handleActionDoTracking(newNetwork);
                        }
                    });

                    mWorker.start();
                } else {
                    synchronized (this) {
                        mTrackingEvent = WifiTrackingEvent.NAME_CHANGE;
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
        WifiTrackingEvent event;

        synchronized (this) {
            mCurrentInRange = isNetworkInRange(network);
            mCurrentNetwork = network;
        }

        while(mWorkerRunning) {
            Date startTime = new Date();
            synchronized (this) {
                while(mTrackingEvent == WifiTrackingEvent.NONE) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        /* do nothing */
                    }
                }
                event = mTrackingEvent;
                mTrackingEvent = WifiTrackingEvent.NONE;
            }

            Date crtTime = new Date();

            switch (event) {
                case NAME_CHANGE:
                    long diffTime = crtTime.getTime() - startTime.getTime();
                    synchronized (this) {
                        if (!mTrackingPaused && mCurrentInRange) {
                            getContentResolver().insert(WifiTimeProvider.TIMES_URI,
                                    WifiTimeProvider.newTimesEntry(mCurrentNetwork, startTime.getTime(),
                                            diffTime));
                        }
                        mCurrentInRange = isNetworkInRange(network);
                        mCurrentNetwork = network;
                    }
                    break;
                case OUT_OF_REACH:
                    diffTime = crtTime.getTime() - startTime.getTime();
                    synchronized (this) {
                        mCurrentInRange = false;
                        if (!mTrackingPaused) {
                            getContentResolver().insert(WifiTimeProvider.TIMES_URI,
                                    WifiTimeProvider.newTimesEntry(mCurrentNetwork, startTime.getTime(),
                                            diffTime));
                        }
                    }
                    break;
                case BACK_IN_RANGE:
                    synchronized (this) {
                        mCurrentInRange = true;
                    }
                    /* do nothing - go to the start of the loop and get a fresh startTime */
                    break;
                case CLEAR_COUNTER:
                    /* do nothing - discard the current time and start fresh */
                    break;
                case TOGGLE_PAUSE:
                    synchronized (this) {
                        mTrackingPaused = !mTrackingPaused;

                        if (mTrackingPaused) {
                            /* backup current value, the network may be out of range when unpausing */
                            diffTime = crtTime.getTime() - startTime.getTime();
                            synchronized (this) {
                                if (mCurrentInRange) {
                                    getContentResolver().insert(WifiTimeProvider.TIMES_URI,
                                            WifiTimeProvider.newTimesEntry(mCurrentNetwork,
                                                    startTime.getTime(), diffTime));
                                }
                            }
                        } else {
                            /* unpausing - do nothing. if not in range anymore, the elapsed time
                            * won't be recorded when the name changes or if we pause again*/
                        }
                    }
                    break;
            }


        }
    }

    private boolean isNetworkInRange(String network) {
        WifiManager wifiManager = (WifiManager)getSystemService(getApplication().WIFI_SERVICE);
        List<ScanResult> list = wifiManager.getScanResults();

        /* getScanResults() returns null is Wifi is disabled */
        return WifiListFragment.listHasNetwork(list, network);
    }
}
