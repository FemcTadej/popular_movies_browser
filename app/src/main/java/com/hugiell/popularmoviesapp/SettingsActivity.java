package com.hugiell.popularmoviesapp;


import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        // Get ListPreference and register preference change listener with it
        ListPreference sortByPreference = (ListPreference) findPreference(getString(R.string.settings_sortBy_key));
        sortByPreference.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // one of the preferences has changed - see which one by its key
        if (preference.getKey().equalsIgnoreCase(getString(R.string.settings_sortBy_key))) {
            ListPreference sortByPreference = (ListPreference) preference;
            int indexOfvalue = sortByPreference.findIndexOfValue(newValue.toString());
            sortByPreference.setSummary(sortByPreference.getEntries()[indexOfvalue]);
        }
        return true;
    }

}
