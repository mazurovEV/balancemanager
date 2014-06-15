package org.onroute.balancemanager;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class BalanceContentProvider extends ContentProvider {
    final String LOG_TAG = "BalanceContentProvider";

    private static final String AUTHORITY = "org.onroute.balancemanager.BalanceContentProvider";

    static final String CONTACT_PATH = "traffic";


    public static final Uri TRAFFIC_CONTENT_URI = Uri.parse("content://"
            + AUTHORITY + "/" + CONTACT_PATH);


    static final String TRAFFIC_CONTENT_TYPE = "vnd.android.cursor.dir/vnd."
            + AUTHORITY + "." + CONTACT_PATH;


    static final int URI_TRAFFIC = 1;


    // описание и создание UriMatcher
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, CONTACT_PATH, URI_TRAFFIC);
    }

    private DbHelper dbHelper;
    private SQLiteDatabase db;

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate");
        dbHelper = new DbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query, " + uri.toString());

        switch (uriMatcher.match(uri)) {
            case URI_TRAFFIC: // общий Uri
                Log.d(LOG_TAG, "URI_TRAFFIC");
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(DbHelper.DATABASE_TABLE, projection, selection,
                selectionArgs, null, null, sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(),
                TRAFFIC_CONTENT_URI);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        Log.d(LOG_TAG, "getType, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_TRAFFIC:
                return TRAFFIC_CONTENT_TYPE;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(LOG_TAG, "insert, " + uri.toString());
        if (uriMatcher.match(uri) != URI_TRAFFIC)
            throw new IllegalArgumentException("Wrong URI: " + uri);

        db = dbHelper.getWritableDatabase();
        long rowID = db.insert(DbHelper.DATABASE_TABLE, null, values);
        Uri resultUri = ContentUris.withAppendedId(TRAFFIC_CONTENT_URI, rowID);

        getContext().getContentResolver().notifyChange(resultUri, null);
        return resultUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "update, " + uri.toString());
        switch (uriMatcher.match(uri)) {
            case URI_TRAFFIC:
                Log.d(LOG_TAG, "URI_TRAFFIC");
                break;
            default:
                throw new IllegalArgumentException("Wrong URI: " + uri);
        }
        db = dbHelper.getWritableDatabase();
        int cnt = db.update(DbHelper.DATABASE_TABLE, values, selection, selectionArgs);

        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }
}
