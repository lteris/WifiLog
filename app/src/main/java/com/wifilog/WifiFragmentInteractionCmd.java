package com.example.liviu.wifilog;

import android.app.Fragment;
import android.os.Bundle;

/**
 * Created by liviu on 25.05.17.
 */


public class WifiFragmentInteractionCmd {
    public Fragment mSource;
    public Command mCommand;
    public Bundle mArguments;

    public enum Command {
        START_TRACKING,
        SHOW_HISTORY
    };

    public final static String START_TRACKING_ARG_NAME = "START_TRACKING_NETWORK";
    public final static String SHOW_HISTORY_NETWORK = "SHOW_HISTORY_NETWORK";

    public WifiFragmentInteractionCmd(Fragment f, Command cmd, Bundle b) {
        mSource = f;
        mCommand = cmd;
        mArguments = b;
    }
}
