package org.onroute.balancemanager;

import android.app.Activity;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;


/**
 * Created by e.mazurov on 08.06.2014.
 */
public class TestActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);
    }

}
