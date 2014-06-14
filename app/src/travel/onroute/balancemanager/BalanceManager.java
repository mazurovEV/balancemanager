package travel.onroute.balancemanager;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class BalanceManager extends Service {

    public static final String MODE = "balance_manager_mode";
    public static final String NEW_LIMIT = "new_limit";
    public static final int START_MANAGE_TRAFFIC = 0;
    public static final int NEW_TRAFFIC_PACK = 1;

    private Handler mHandler = new Handler();
    private Traffic mTraffic;
    private long mLimit;
    private int mHandleInterval;

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        mHandleInterval = Integer.valueOf(pref.getString("handle_traffic", "1000"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getIntExtra(MODE, 0)) {
            case START_MANAGE_TRAFFIC:
                sendUnblockingIntnet();
                sendTrafficRequest();
                Log.e("BalanceManager", "in fact, limit = " + mLimit);
                getCachedTrafic();
                Log.e("BalanceManager", "cachedTraffic: " + mTraffic);
                mHandler.postDelayed(mRunnable, mHandleInterval);
                break;
            case NEW_TRAFFIC_PACK:
                sendUnblockingIntnet();
                mHandler.removeCallbacks(mRunnable);
                updateLimit(intent.getIntExtra(NEW_LIMIT, 0));
                mHandler.postDelayed(mRunnable, mHandleInterval);
        }
        return START_STICKY;
    }

    private void updateLimit(int limit) {
        mLimit = limit;
        ContentValues v = new ContentValues();
        v.put(DbHelper.LIMIT_COLUMN, limit);
        v.put(DbHelper.RECEIVED_COLUMN, 0);
        v.put(DbHelper.TRANSFER_COLUMN, 0);
        mTraffic = new Traffic(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes(), 0, 0);
        updateOrInsert(v);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void sendTrafficRequest() {
        Cursor c = getContentResolver().query(BalanceContentProvider.TRAFFIC_CONTENT_URI, new String[] {DbHelper.LIMIT_COLUMN}, null, null, null);
        mLimit = c.moveToFirst() ? c.getLong(c.getColumnIndex(DbHelper.LIMIT_COLUMN)) : 0;
        Log.e("BalanceManager", "limit from db = " + mLimit);
        if(mLimit == 0) {
            try {
                mLimit = new TrafficLimitRequest().execute().get();
                Log.e("BalanceManager", "limit from request= " + mLimit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private void getCachedTrafic() {
        Cursor c = getContentResolver().query(BalanceContentProvider.TRAFFIC_CONTENT_URI, new String[] {DbHelper.RECEIVED_COLUMN, DbHelper.TRANSFER_COLUMN}, null, null, null);
        mTraffic = c.moveToFirst()
                ? new Traffic(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes(), c.getLong(c.getColumnIndex(DbHelper.RECEIVED_COLUMN)), c.getLong(c.getColumnIndex(DbHelper.TRANSFER_COLUMN)))
                : new Traffic(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes(), 0, 0);
    }


    private final Runnable mRunnable = new Runnable() {
        public void run() {
            mTraffic.addTraffic(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes());
            saveTraffic();
            if(!sendBlockingIntent()) {
                mHandler.postDelayed(mRunnable, mHandleInterval);
            }
        }
    };

    private void saveTraffic() {
        ContentValues v = new ContentValues();
        v.put(DbHelper.RECEIVED_COLUMN, mTraffic.getReceivedBytes());
        v.put(DbHelper.TRANSFER_COLUMN, mTraffic.getTransferBytes());
        v.put(DbHelper.ALL_COLUMN, mTraffic.getReceivedBytes() + mTraffic.getTransferBytes());
        updateOrInsert(v);
    }

    private void sendUnblockingIntnet() {
        Toast.makeText(this, "send UnblockingIntent", Toast.LENGTH_LONG).show();
         /* Intent intent = new Intent();
            intent.setClassName("com.prestigio.launcher.mdm", "com.prestigio.launcher.mdm.MdmLauncherActivity");
            intent.putExtra("enable", true); // enable
            intent.putExtra("disable", false); // enable
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/

        ContentValues v = new ContentValues();
        v.put(DbHelper.STATUS_COLUMN, 1);
        updateOrInsert(v);
    }

    private boolean sendBlockingIntent() {
        if(mTraffic.getAll() > mLimit) {
            Toast.makeText(this, "send BlockingIntent", Toast.LENGTH_LONG).show();
           /* Intent intent = new Intent();
            intent.setClassName("com.prestigio.launcher.mdm", "com.prestigio.launcher.mdm.MdmLauncherActivity");
            intent.putExtra("enable", false); // disable
            intent.putExtra("disable", true); // disable
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);*/

            ContentValues v = new ContentValues();
            v.put(DbHelper.STATUS_COLUMN, 0);
            updateOrInsert(v);

            return true;
        }

        return false;
    }

    private void updateOrInsert(ContentValues v) {
        int updateRowId = getContentResolver().update(BalanceContentProvider.TRAFFIC_CONTENT_URI, v, null, null);
        if(updateRowId <= 0) {
            getContentResolver().insert(BalanceContentProvider.TRAFFIC_CONTENT_URI, v);
        }
    }

    private class TrafficLimitRequest extends AsyncTask<Void, Void, Long> {

        @Override
        protected Long doInBackground(Void... params) {
            //TODO: запрос доступного трафика
            Log.d("BalanceManager", "send limit request");
            return 10*1024*1024L;
        }

        @Override
        protected void onPostExecute(Long limit) {
            super.onPostExecute(limit);
            //(не будет ли проблем с асинхронностью?)
            mLimit = limit;
            ContentValues v = new ContentValues();
            v.put(DbHelper.LIMIT_COLUMN, limit);//Не понятно как понять что надо сбросить значения траффика
            updateOrInsert(v);
        }
    }
}
