package org.onroute.balancemanager;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class DbHelper extends SQLiteOpenHelper implements BaseColumns {

    private static final String DATABASE_NAME = "balance_manager.db";
    private static final int DATABASE_VERSION = 1;


    static final String DATABASE_TABLE = "traffic";
    static final String LIMIT_COLUMN = "limit_traffic";
    static final String TRANSFER_COLUMN = "transfer";
    static final String RECEIVED_COLUMN = "received";
    static final String INIT_TRANSFER_COLUMN = "init_transfer";
    static final String INIT_RECEIVED_COLUMN = "init_received";
    static final String ALL_COLUMN = "all_traffic";
    static final String STATUS_COLUMN = "status_traffic";

    private static final String DATABASE_CREATE_SQL = "create table "
            + DATABASE_TABLE + " ("+ LIMIT_COLUMN + " integer, " + INIT_TRANSFER_COLUMN + " integer, " + INIT_RECEIVED_COLUMN
            + " integer, " + RECEIVED_COLUMN + " integer, " + TRANSFER_COLUMN + " integer, " + STATUS_COLUMN
            + " integer DEFAULT 0, " + ALL_COLUMN + " integer);";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void updateLimit(long limit) {
        //TODO: обновить поле limit и сбросить все остальные поля
    }

    public void updateTraffic(long received, long transfer) {
        //TODO: обновлять поля received и transfer + считать и обновлять all
    }

    public long getLimit() {
       return 0;
    }

    public long getTraffic() {
        return 0;
    }
}
