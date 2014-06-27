package org.onroute.balancemanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;


/**
 * Created by e.mazurov on 08.06.2014.
 */
public class TestActivity extends Activity {

    private String imei;
    private String imsi;
    private String locale;
    private EditText pin;
    private boolean isNew;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
        setContentView(R.layout.pin_layout);

        saveInitialData();

        pin = (EditText) findViewById(R.id.pin);

        Button newTraffic = (Button) findViewById(R.id.new_traffic);
        newTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew = true;
                new UpdateTrafficRequest().execute();
            }
        });


        Button addTraffic = (Button) findViewById(R.id.add_traffic);
        addTraffic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isNew = false;
                new UpdateTrafficRequest().execute();
            }
        });


        Button seeBalanceManager = (Button) findViewById(R.id.start_balance_manager);
        seeBalanceManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TestActivity.this, UserActivity.class));
            }
        });
    }

    private void saveInitialData() {
        TelephonyManager tm =(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();
        imsi = tm.getSubscriberId();

        Locale current = getResources().getConfiguration().locale;
        locale = current.getISO3Language();
        Log.d("BalanceManager", "imei = " + imei + " imsi = " + imsi + " locale = " + locale);
    }

    private class UpdateTrafficRequest extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            URL url;
            HttpURLConnection connection = null;
            try {
                //Create connection
                url = new URL("http://www.avk-billing.ru/onroute/voucher.php");
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");


                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream output = connection.getOutputStream();
                output.write(getJsonParams().getBytes("UTF-8"));

                connection.connect();
                int statusCode = connection.getResponseCode();
                if(statusCode == 200) {
                    //Get Response
                    InputStream is = connection.getInputStream();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer response = new StringBuffer();
                    while ((line = rd.readLine()) != null) {
                        response.append(line);
                        response.append('\r');
                    }
                    rd.close();
                    return response.toString();
                } else {
                    return "";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if(connection != null) {
                    connection.disconnect();
                }
            }
        }

        @Override
        //{"action":"accept","message":"","limit":5120,"update_perc":10,"update_time":3600,"allowance_end":36000}
        protected void onPostExecute(String s) {
            if(TextUtils.isEmpty(s)) return;
            long limit = 0;
            try {
                JSONObject o = new JSONObject(s);
                if(o.getString("action").equals("accept")) {
                    limit = o.getLong("limit");
                    Toast.makeText(TestActivity.this, "new limit = " + limit, Toast.LENGTH_SHORT).show();
                } else if(o.getString("action").equals("reject")) {
                    Toast.makeText(TestActivity.this, "", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(TestActivity.this, "", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("TestActivity", s);
            Intent i = new Intent(TestActivity.this, BalanceManager.class);
            if(isNew) {
                i.putExtra(BalanceManager.MODE, BalanceManager.NEW_TRAFFIC_PACK);
            } else {
                i.putExtra(BalanceManager.MODE, BalanceManager.ADD_TRAFFIC_PACK);
            }
            i.putExtra(BalanceManager.NEW_LIMIT, limit);
            startService(i);
        }
    }

    private String getJsonParams() {
        JSONObject o = new JSONObject();
        try {
            o.put("action", "activation").put("IMSI", imsi).put("IMEI", imei).put("locale", locale).put("PIN", pin.getText().toString()).
                    put("is_new", isNew ? "1" : "0").put("time_zone", 240).put("version", "0001").put("current_time", System.currentTimeMillis()).
                    put("tran_id", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return o.toString();
    }

}
