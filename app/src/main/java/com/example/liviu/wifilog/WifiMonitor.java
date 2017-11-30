package com.example.liviu.wifilog;

import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;


public class WifiMonitor extends AppCompatActivity implements WifiListFragment.OnFragmentInteractionListener,
        WifiTrackFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_monitor);

        if (findViewById(R.id.activity_wifi_monitor) != null) {

            if (savedInstanceState != null) {
                return;
            }

            WifiListFragment wifiListFragment = new WifiListFragment();

            wifiListFragment.setArguments(getIntent().getExtras());

            getFragmentManager().beginTransaction()
                    .add(R.id.activity_wifi_monitor, wifiListFragment).commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onListFragmentInteraction(WifiFragmentInteractionCmd cmd) {
        if(cmd.mSource instanceof WifiListFragment) {
            switch (cmd.mCommand) {
                case START_TRACKING:
                    String net = cmd.mArguments.
                            getString(WifiFragmentInteractionCmd.START_TRACKING_ARG_NAME);
                    WifiTrackFragment frag = WifiTrackFragment.newInstance(net);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.replace(R.id.activity_wifi_monitor, frag);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;

                default:
                    break;
            }
        }
    }

    /* commands coming from WifiTrackFragment */
    public void onTrackFragmentInteraction(WifiFragmentInteractionCmd cmd) {
        if (cmd.mSource instanceof WifiTrackFragment) {
            switch (cmd.mCommand) {
                case SHOW_HISTORY:
                    String net = cmd.mArguments.
                            getString(WifiFragmentInteractionCmd.SHOW_HISTORY_NETWORK);
                    WifiHistoryFragment frag = WifiHistoryFragment.newInstance(net);
                    FragmentTransaction transaction = getFragmentManager().beginTransaction();

                    transaction.replace(R.id.activity_wifi_monitor, frag);
                    transaction.addToBackStack(null);
                    transaction.commit();
                    break;
                default:
                    break;
            }
        }
    }
}
