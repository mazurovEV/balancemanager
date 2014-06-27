package org.onroute.balancemanager;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

/**
 * Created by e.mazurov on 25.06.2014.
 */
public class UserActivity extends Activity implements PasswordDialog.PassDialogListener {

    private static final int DIALOG_SETTINGS = 1;
    private static final int DIALOG_NEW_TRAFFIC = 0;

    private Handler mHandler = new Handler();
    private int passDialogMode = -1;
    private EditText extraTraffic;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        setContentView(R.layout.main);

        Button addTraffic = (Button) findViewById(R.id.button_add_traffic);
        extraTraffic = (EditText) findViewById(R.id.add_traffic_pack);

        addTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDialogMode = DIALOG_NEW_TRAFFIC;
                showEditDialog();
            }
        });

        Button settings = (Button) findViewById(R.id.button_settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passDialogMode = DIALOG_SETTINGS;
                showEditDialog();
            }
        });

        Intent i = new Intent(UserActivity.this, BalanceManager.class);
        i.putExtra(BalanceManager.MODE, BalanceManager.START_MANAGE_TRAFFIC);
        startService(i);
        mHandler.postDelayed(mRunnable, getInterval());
    }

    private int getInterval() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        return Integer.valueOf(pref.getString("handle_traffic", "1000"));
    }

    private void showEditDialog() {
        FragmentManager fm = getFragmentManager();
        PasswordDialog passDialog = new PasswordDialog();
        passDialog.show(fm, "fragment_password");
    }

    @Override
    public void onFinishEditDialog(boolean isPassOk) {
        if(isPassOk) {
            if(passDialogMode == DIALOG_SETTINGS) {
                startActivity(new Intent(UserActivity.this, SettingsActivity.class));
            } else if(passDialogMode == DIALOG_NEW_TRAFFIC) {
                Intent i = new Intent(UserActivity.this, BalanceManager.class);
                i.putExtra(BalanceManager.MODE, BalanceManager.NEW_TRAFFIC_PACK);
                i.putExtra(BalanceManager.NEW_LIMIT, Integer.valueOf(extraTraffic.getText().toString())*1024*1024);
                startService(i);
            }
        } else {
            Toast.makeText(this, "Wrong password", Toast.LENGTH_SHORT).show();
        }
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            Cursor c = getContentResolver().query(BalanceContentProvider.TRAFFIC_CONTENT_URI, new String[] {DbHelper.RECEIVED_COLUMN, DbHelper.TRANSFER_COLUMN, DbHelper.STATUS_COLUMN}, null, null, null);
            boolean hasData = c.moveToFirst();
            TextView RX = (TextView)findViewById(R.id.RX);
            TextView TX = (TextView)findViewById(R.id.TX);
            TextView status = (TextView)findViewById(R.id.status);
            RX.setText(Long.toString(hasData ? c.getLong(c.getColumnIndex(DbHelper.RECEIVED_COLUMN)) : 0));
            TX.setText(Long.toString(hasData ? c.getLong(c.getColumnIndex(DbHelper.TRANSFER_COLUMN)) : 0));
            status.setText("(" + (c.getLong(c.getColumnIndex(DbHelper.STATUS_COLUMN)) == 0 ? "Off" : "On") + ")");
            c.close();
            mHandler.postDelayed(mRunnable, getInterval());
        }
    };

}

