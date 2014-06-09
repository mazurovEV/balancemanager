package travel.onroute.balancemanager;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import travel.onroute.BalanceManager.R;


/**
 * Created by e.mazurov on 08.06.2014.
 */
public class TestActivity extends Activity {
    private Handler mHandler = new Handler();
    private Traffic mTraffic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startService(new Intent(this, BalanceManager.class));
        mHandler.postDelayed(mRunnable, 1000);
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            Cursor c = getContentResolver().query(BalanceContentProvider.TRAFFIC_CONTENT_URI, new String[] {DbHelper.RECEIVED_COLUMN, DbHelper.TRANSFER_COLUMN}, null, null, null);
            boolean hasData = c.moveToFirst();
            TextView RX = (TextView)findViewById(R.id.RX);
            TextView TX = (TextView)findViewById(R.id.TX);
            RX.setText(Long.toString(hasData ? c.getLong(c.getColumnIndex(DbHelper.RECEIVED_COLUMN)) : 0));
            TX.setText(Long.toString(hasData ? c.getLong(c.getColumnIndex(DbHelper.TRANSFER_COLUMN)) : 0));
            mHandler.postDelayed(mRunnable, 1000);
        }
    };

}
