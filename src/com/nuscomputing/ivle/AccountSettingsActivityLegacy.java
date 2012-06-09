package com.nuscomputing.ivle;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Main settings activity (for Android < 3.0).
 * Allows users to view and update preferences for NUS IVLE.
 * @author yjwong
 */
public class AccountSettingsActivityLegacy extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.layout.account_settings_activity_legacy);
    }
}
