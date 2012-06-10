package com.nuscomputing.ivle;

import android.app.Activity;
import android.os.Bundle;

/**
 * Main settings activity.
 * Allows users to view and update preferences for NUS IVLE.
 * @author yjwong
 */
public class SettingsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
    }
}