package com.nuscomputing.ivle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

import android.os.Bundle;

/**
 * Main settings activity (for Android < 3.0).
 * Allows users to view and update preferences for NUS IVLE.
 * @author yjwong
 */
public class AccountSettingsActivityLegacy extends SherlockPreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.account_settings_activity_legacy);
    }
}
