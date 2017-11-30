package com.example.liviu.wifilog;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;


public class WifiTrackFragment extends Fragment implements View.OnClickListener {
    private static final String ARG_NETWORK_NAME = "network_name";

    private TextView wifiNetworkName;
    private Chronometer wifiChronoView;
    private Button wifiButtonToggleChrono;
    private Button wifiButtonResetChrono;
    private Button wifiButtonHistory;

    private boolean mChronoRunning = false;
    private String mNetworkName;
    private WifiTrackService mTrackService = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mTrackService = ((WifiTrackService.WifiTrackingBinder)service).getServiceInstance();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mTrackService = null;
        }
    };

    private OnFragmentInteractionListener mListener;

    public WifiTrackFragment() {
        // Required empty public constructor
    }

    public static WifiTrackFragment newInstance(String name) {
        WifiTrackFragment fragment = new WifiTrackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_NETWORK_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mNetworkName = getArguments().getString(ARG_NETWORK_NAME);
        }

        WifiTrackService.startTracking(getActivity(), mNetworkName);

        /* bind to the new service */
        bindTrackingService();
    }

    @Override
    public void onDestroy() {
        /* unbind service - started service so it should continue to run */
        unbindTrackingService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifi_track, container, false);
        wifiChronoView = (Chronometer) view.findViewById(R.id.chronoWifi);
        wifiChronoView.setBase(SystemClock.elapsedRealtime());
        wifiChronoView.start();
        mChronoRunning = true;

        wifiNetworkName = (TextView) view.findViewById(R.id.textWifiName);
        wifiNetworkName.setText(mNetworkName);

        wifiButtonToggleChrono = (Button) view.findViewById(R.id.buttonToggleChrono);
        wifiButtonToggleChrono.setText("Start");
        wifiButtonToggleChrono.setOnClickListener(this);

        wifiButtonResetChrono = (Button) view.findViewById(R.id.buttonResetChrono);
        wifiButtonResetChrono.setOnClickListener(this);

        wifiButtonHistory = (Button) view.findViewById(R.id.buttonShowHistory);
        wifiButtonHistory.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonToggleChrono:
                clockToggle();
                break;
            case R.id.buttonResetChrono:
                clockReset();
                break;
            case R.id.buttonShowHistory:
                showHistory();
                break;
            default:
                Log.d("CIUCH", "Wrong button");
        }
    }

    public interface OnFragmentInteractionListener {
        void onTrackFragmentInteraction(WifiFragmentInteractionCmd cmd);
    }

    private void clockToggle() {
        if (mTrackService == null)
            return;

        wifiButtonToggleChrono.setText(mChronoRunning ? "START" : "STOP");

        if (mChronoRunning) {
            wifiChronoView.stop();
        } else {
            wifiChronoView.start();
        }

        mChronoRunning = !mChronoRunning;
        if (mTrackService != null) {
            mTrackService.pauseRecording();
        } else {
            Toast.makeText(getActivity(), "Cannot record wifi log. " +
                    "Got disconencted from the trackin service", Toast.LENGTH_SHORT).show();
        }
    }

    private void clockReset() {
        wifiChronoView.setBase(SystemClock.elapsedRealtime());
        if (mTrackService != null) {
            mTrackService.clearRecording();
        } else {
            Toast.makeText(getActivity(), "Cannot record wifi log. " +
                    "Got disconencted from the trackin service", Toast.LENGTH_SHORT).show();
        }
    }

    private void showHistory() {
        Bundle b = new Bundle();
        b.putString(WifiFragmentInteractionCmd.SHOW_HISTORY_NETWORK,"");
        mListener.onTrackFragmentInteraction(new WifiFragmentInteractionCmd(this,
                WifiFragmentInteractionCmd.Command.SHOW_HISTORY, b));
    }

    private void bindTrackingService() {
        Intent intent = new Intent(getActivity(), WifiTrackService.class);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindTrackingService() {
        getActivity().unbindService(mConnection);
    }
}
