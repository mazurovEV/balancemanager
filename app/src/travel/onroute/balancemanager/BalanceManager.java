package travel.onroute.balancemanager;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

/**
 * Created by e.mazurov on 08.06.2014.
 */
public class BalanceManager extends Service {

    private Handler mHandler = new Handler();
    private Traffic mTraffic;
    private long mLimit;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendTrafficRequest();
        Log.e("BalanceManager", "in fact, limit = " + mLimit);
        getCachedTrafic();
        Log.e("BalanceManager", "cachedTraffic: " + mTraffic);
        mHandler.postDelayed(mRunnable, 1000);
        return START_STICKY;
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
        mTraffic = c.moveToFirst() ? new Traffic(c.getLong(c.getColumnIndex(DbHelper.RECEIVED_COLUMN)), c.getLong(c.getColumnIndex(DbHelper.RECEIVED_COLUMN))) : new Traffic(0, 0);
    }


    private final Runnable mRunnable = new Runnable() {
        public void run() {
            mTraffic.addTraffic(TrafficStats.getTotalRxBytes(), TrafficStats.getTotalTxBytes());
            saveTraffic();
            sendBlockingIntent();
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

    private void saveTraffic() {
        ContentValues v = new ContentValues();
        v.put(DbHelper.RECEIVED_COLUMN, mTraffic.getReceivedBytes());
        v.put(DbHelper.TRANSFER_COLUMN, mTraffic.getTransferBytes());
        v.put(DbHelper.ALL_COLUMN, mTraffic.getReceivedBytes() + mTraffic.getTransferBytes());
        updateOrInsert(v);
    }

    private void sendBlockingIntent() {
        if(mTraffic.getAll() > mLimit) {
            Toast.makeText(this, "send BlockingIntent", Toast.LENGTH_LONG).show();
            Intent intent = new Intent();
            intent.setClassName("com.prestigio.launcher.mdm", "com.prestigio.launcher.mdm.MdmLauncherActivity");
            //intent.putExtra("enable", true); // enable
            intent.putExtra("enable", false); // disable
            intent.putExtra("disable", true); // disable
            //intent.putExtra("disable", false); // enable
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
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
