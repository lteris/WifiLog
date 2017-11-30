package com.example.liviu.wifilog;

import android.content.ContentResolver;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liviu.wifilog.WifiHistoryFragment.OnFragmentInteractionListener;

import java.util.LinkedList;
import java.util.List;


public class WifiHistoryViewAdapter extends RecyclerView.Adapter<WifiHistoryViewAdapter.ViewHolder> {

    private final List<WifiHistoryFragment.TimeEntryItem> mValues;
    private final OnFragmentInteractionListener mListener;

    public WifiHistoryViewAdapter(ContentResolver resolver, String network,
                                  OnFragmentInteractionListener listener) {
        mValues = getTimeEntries(network, resolver);
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).mName);
        holder.mContentView.setText(mValues.get(position).mStartTime + "||" +
                mValues.get(position).mDuration);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onHistoryFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;

        public WifiHistoryFragment.TimeEntryItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
    /* >>>>>>>>>>>>>>>> Get items from the content provider >>>>>>>>>>>>>>>>> */


    private static List<WifiHistoryFragment.TimeEntryItem>
                                    getTimeEntries(String network, ContentResolver resolver) {

        List<WifiHistoryFragment.TimeEntryItem> ret = new LinkedList<>();

        Cursor cursor = resolver.query(WifiTimeProvider.TIMES_URI,
                new String[]{WifiTimeProvider.WifiDatabaseHelper.TIMES_WIFI_NAME,
                        WifiTimeProvider.WifiDatabaseHelper.TIMES_START_TIME,
                        WifiTimeProvider.WifiDatabaseHelper.TIMES_DURATION}, null,
                        new String[]{""}, null); //TODO filter by network

        if (cursor == null) {
            Log.d("ERROR", "No records available");
            return ret;
        }

        int col_wifi_name_index =
                cursor.getColumnIndex(WifiTimeProvider.WifiDatabaseHelper.TIMES_WIFI_NAME);
        int col_start_index =
                cursor.getColumnIndex(WifiTimeProvider.WifiDatabaseHelper.TIMES_WIFI_NAME);
        int col_duration_index =
                cursor.getColumnIndex(WifiTimeProvider.WifiDatabaseHelper.TIMES_WIFI_NAME);

        while (cursor.moveToNext()) {
            ret.add(new WifiHistoryFragment.TimeEntryItem(cursor.getString(col_wifi_name_index),
                    cursor.getLong(col_start_index), cursor.getLong(col_duration_index)));
        }

        cursor.close();

        return ret;
    }
}
