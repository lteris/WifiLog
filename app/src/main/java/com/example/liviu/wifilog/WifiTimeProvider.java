package com.example.liviu.wifilog;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.List;


/**
 * Simple ContentProvider for the times recorded by the WifiTrackService.
 * Contains 2 tables:
 * 1. Times [CHUNK_START_TIME(Unix time), CHUNK_DURATION]
 * 2. Scores[DAY_START(Unix time), CRITERIUM1, CRITERIUM2 ...]
 */

public class WifiTimeProvider extends ContentProvider {
    private static final int DB_VERSION = 2;
    private static final String DB_NAME = "dbWifiTimes";
    private static final String TABLE_TIMES_NAME = "TTimes";
    private static final String TABLE_SCORES_NAME = "TScores";

    private static final String TABLE_TIMES_MIME =
            "vnd.android.cursor.dir/vnd.com.example.provider.TTimes";
    private static final String TABLE_SCORES_MIME =
            "vnd.android.cursor.dir/vnd.com.example.provider.TScores";

    private static final String URI_SCHEME = "content://";
    private static final String URI_AUTHORITY = "WIFI_TRACKER";

    public static final Uri TIMES_URI =
            Uri.parse(URI_SCHEME + URI_AUTHORITY + "/" + TABLE_TIMES_NAME);
    public static final Uri SCORES_URI =
            Uri.parse(URI_SCHEME + URI_AUTHORITY + "/" + TABLE_SCORES_NAME);

    private static final int TABLE_TIMES_URI_NO = 1;
    private static final int TABLE_SCORES_URI_NO = 2;
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Initialize the UriMatcher for our tables
     */
    static {
        sUriMatcher.addURI(URI_AUTHORITY, TABLE_TIMES_NAME, TABLE_TIMES_URI_NO);
        sUriMatcher.addURI(URI_AUTHORITY, TABLE_SCORES_NAME, TABLE_SCORES_URI_NO);
    }

    private SQLiteDatabase mDatabase;

    protected static final class WifiDatabaseHelper extends SQLiteOpenHelper {
        public static final String TIMES_WIFI_NAME = "NAME";
        public static final String TIMES_START_TIME = "START_TIME";
        public static final String TIMES_DURATION = "DURATION";

        public static final String SCORES_DAY = "DAY";
        public static final String SCORES_CODE = "SCORE_CODE";
        public static final String SCORES_DEBUG = "SCORE_DEBUG";
        public static final String SCORES_TECH = "SCORE_TECH";

        private static final String SQL_CREATE_TIMES_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_TIMES_NAME +
                "(" +
                TIMES_WIFI_NAME + " TEXT, " +
                TIMES_START_TIME + " INTEGER, " +
                TIMES_DURATION + " INTEGER" +
                " , PRIMARY KEY (" + TIMES_WIFI_NAME  + " , " + TIMES_DURATION + ")" +
                ")";

        private static final String SQL_CREATE_TIMES_INDEX = "CREATE INDEX IF NOT EXISTS " +
                TABLE_TIMES_NAME + "INDEX" + " ON " + TABLE_TIMES_NAME +
                "(" +
                TIMES_WIFI_NAME +
                ")";

        private static final String SQL_CREATE_SCORES_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_SCORES_NAME +
                "(" +
                SCORES_DAY + " INTEGER PRIMARY KEY, " +
                SCORES_CODE + " INTEGER, " +
                SCORES_DEBUG + " INTEGER, " +
                SCORES_TECH + " INTEGER" +
                ")";

        private static final String SQL_CREATE_SCORES_INDEX = "CREATE INDEX IF NOT EXISTS " +
                TABLE_SCORES_NAME + "INDEX" + " ON " + TABLE_SCORES_NAME +
                "(" +
                SCORES_DAY +
                ")";

        private static final String SQL_DROP_TIMES_TABLE = "DROP TABLE " + TABLE_TIMES_NAME;


        public WifiDatabaseHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TIMES_TABLE);
            db.execSQL(SQL_CREATE_SCORES_TABLE);
            db.execSQL(SQL_CREATE_TIMES_INDEX);
            db.execSQL(SQL_CREATE_SCORES_INDEX);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion == 2) {
                db.execSQL(SQL_DROP_TIMES_TABLE);
                db.execSQL(SQL_CREATE_TIMES_TABLE);
            }
        }
    }

    public static ContentValues newTimesEntry(String name, long startTime, long duration) {
        ContentValues values = new ContentValues();

        values.put(WifiDatabaseHelper.TIMES_WIFI_NAME, name);
        values.put(WifiDatabaseHelper.TIMES_START_TIME, startTime);
        values.put(WifiDatabaseHelper.TIMES_DURATION, duration);

        return values;
    }

    public static ContentValues newScoresEntry(long day, long code, long debug, long tech) {
        ContentValues values = new ContentValues();

        values.put(WifiDatabaseHelper.SCORES_DAY, day);
        values.put(WifiDatabaseHelper.SCORES_CODE, code);
        values.put(WifiDatabaseHelper.SCORES_DEBUG, debug);
        values.put(WifiDatabaseHelper.SCORES_TECH, tech);

        return values;
    }

    public boolean onCreate() {
        mDatabase = (new WifiDatabaseHelper(getContext())).getWritableDatabase();

        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder){
        switch(sUriMatcher.match(uri)) {
            case TABLE_TIMES_URI_NO:
                return mDatabase.query(TABLE_TIMES_NAME, projection, selection, selectionArgs,
                        null, null, null);
            case TABLE_SCORES_URI_NO:
                return mDatabase.query(TABLE_SCORES_NAME, projection, selection, selectionArgs,
                        null, null, null);
            default:
                return null;
        }
    }

    public int update(Uri uri, ContentValues values,
               String selection, String[] selectionArgs) {
        switch(sUriMatcher.match(uri)) {
            case TABLE_TIMES_URI_NO:
                return mDatabase.update(TABLE_TIMES_NAME, values, selection, selectionArgs);
            case TABLE_SCORES_URI_NO:
                return mDatabase.update(TABLE_SCORES_NAME, values, selection, selectionArgs);
            default:
                return 0;
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        switch (sUriMatcher.match(uri)) {
            case TABLE_TIMES_URI_NO:
                long rowId = mDatabase.insert(TABLE_TIMES_NAME, null, values);
                return ContentUris.withAppendedId(TIMES_URI, rowId);
            case TABLE_SCORES_URI_NO:
                rowId = mDatabase.insert(TABLE_SCORES_NAME, null, values);
                return ContentUris.withAppendedId(SCORES_URI, rowId);
            default:
                return null;
        }
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch(sUriMatcher.match(uri)) {
            case TABLE_TIMES_URI_NO:
                return mDatabase.delete(TABLE_TIMES_NAME, selection, selectionArgs);
            case TABLE_SCORES_URI_NO:
                return mDatabase.delete(TABLE_SCORES_NAME, selection, selectionArgs);
            default:
                return 0;
        }
    }

    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case TABLE_TIMES_URI_NO:
                return TABLE_TIMES_MIME;
            case TABLE_SCORES_URI_NO:
                return TABLE_SCORES_MIME;
            default:
                return null;
        }
    }

}
