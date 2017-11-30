package com.example.liviu.wifilog;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class WifiListFragment extends Fragment implements View.OnClickListener{
    private OnFragmentInteractionListener mListener;

    //TODO - remove this
    private static final boolean TEST = Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk".equals(Build.PRODUCT);

    private ListView wifiListView;
    private TextView wifiTextView;

    private ArrayList<String> mNamesList;
    private ArrayAdapter<String> mListAdapter;

    private static String scanResultToString(ScanResult r) {
        return r.SSID + "||" + r.BSSID.toString();
    }

    public static boolean listHasNetwork(List<ScanResult> list, String network) {
        if (list == null || network == null)
            return false;

        for (ScanResult sr: list) {
            if (scanResultToString(sr).equals(network)) {
                return true;
            }
        }

        return false;
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(context.WIFI_SERVICE);
            List<ScanResult> list = wifiManager.getScanResults();

            mNamesList.clear();

            for (ScanResult r: list) {
                mNamesList.add(scanResultToString(r));
            }

            mListAdapter.notifyDataSetChanged();
        }
    };

    public WifiListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_list, container, false);

        wifiListView = (ListView)view.findViewById(R.id.listWifiDetected);
        wifiTextView = (TextView)view.findViewById(R.id.editWifiName);

        Button b = (Button)view.findViewById(R.id.buttonTrack);
        b.setOnClickListener(this);

        mNamesList = new ArrayList<String>();
        mListAdapter = new ArrayAdapter<String>(container.getContext(),
                android.R.layout.simple_list_item_1, mNamesList);
        wifiListView.setAdapter(mListAdapter);

        if (TEST) {
            fillTest();
        }

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
        getActivity().registerReceiver(mWifiReceiver, filters);

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onDestroy()
    {
        if (mWifiReceiver != null) {
            getActivity().unregisterReceiver(mWifiReceiver);
            mWifiReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onAttach(Activity context)
    {
        Log.d("DEBUG", "Calling on attach activity");
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Context context) {
        Log.d("DEBUG", "Calling on attach");
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onListFragmentInteraction(WifiFragmentInteractionCmd cmd);
    }



    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.buttonTrack:
                if (wifiTextView.getText().toString() != "") {
                    Bundle b = new Bundle();
                    b.putString(WifiFragmentInteractionCmd.START_TRACKING_ARG_NAME,
                            wifiTextView.getText().toString());
                    mListener.onListFragmentInteraction(new WifiFragmentInteractionCmd(this,
                            WifiFragmentInteractionCmd.Command.START_TRACKING, b));
                }
                break;
            default:
                Log.d("CIUCH", "Wrong button");
        }
    }

    private void fillTest()
    {
        for (int i = 0; i < 10; i++) {
            mNamesList.add("TEST" + i);
        }
    }
}
