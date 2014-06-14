package org.onroute.balancemanager;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by e.mazurov on 14.06.2014.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
    }
}
