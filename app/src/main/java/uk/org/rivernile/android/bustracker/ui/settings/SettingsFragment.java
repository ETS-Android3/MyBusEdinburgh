/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.support.v7.preference.PreferenceFragmentCompat;

import uk.org.rivernile.android.bustracker.preferences.PreferenceConstants;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link PreferenceFragment} allows the user to change app-wide preferences.
 *
 * @author Niall Scott
 */
public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String DIALOG_TAG = "dialog";

    private SharedPreferences sp;
    private ListPreference numberOfDeparturesPref;
    private String[] numberOfDeparturesStrings;

    @Override
    public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
        getPreferenceManager().setSharedPreferencesName(PreferenceConstants.PREF_FILE);
        addPreferencesFromResource(R.xml.preferences);
        sp = getPreferenceScreen().getSharedPreferences();
        numberOfDeparturesStrings = getResources()
                .getStringArray(R.array.preferences_num_departures_entries);
        numberOfDeparturesPref = (ListPreference)
                findPreference(PreferenceConstants.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE);
    }

    @Override
    public void onStart() {
        super.onStart();

        sp.registerOnSharedPreferenceChangeListener(this);
        populateNumberOfDeparturesSummary();
    }

    @Override
    public void onStop() {
        super.onStop();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDisplayPreferenceDialog(final Preference preference) {
        final String key = preference.getKey();
        final PreferenceDialogFragmentCompat f;

        if (PreferenceConstants.PREF_BACKUP_FAVOURITES.equals(key)) {
            f = BackupPreferenceDialogFragment.newInstance(key);
        } else if (PreferenceConstants.PREF_RESTORE_FAVOURITES.equals(key)) {
            f = RestorePreferenceDialogFragment.newInstance(key);
        } else if (PreferenceConstants.PREF_CLEAR_MAP_SEARCH_HISTORY.equals(key)) {
            f = ClearSearchHistoryPreferenceDialogFragment.newInstance(key);
        } else {
            f = null;
        }

        if (f != null) {
            f.setTargetFragment(this, 0);
            f.show(getFragmentManager(), DIALOG_TAG);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sp, final String key) {
        if (PreferenceConstants.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE.equals(key)) {
            populateNumberOfDeparturesSummary();
        }
    }

    /**
     * Populate the summary text for number of departures.
     */
    private void populateNumberOfDeparturesSummary() {
        final String s = sp.getString(PreferenceConstants
                .PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE, "4");
        int val;

        try {
            val = Integer.parseInt(s);
        } catch(NumberFormatException e) {
            val = 4;
        }

        numberOfDeparturesPref.setSummary(numberOfDeparturesStrings[val - 1]);
    }
}
