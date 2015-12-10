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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.widget.Toast;

import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

/**
 * Shows a {@link PreferenceDialogFragmentCompat} asking the user to confirm if they wish to restore
 * their favourites from external storage. If the user confirms, then the restore process is carried
 * out.
 *
 * @author Niall Scott
 */
public class RestorePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private SettingsDatabase sd;

    /**
     * Create a new instance of this {@link PreferenceDialogFragmentCompat}.
     *
     * @param key The key of the {@link android.support.v7.preference.Preference}.
     * @return A new instance of this {@link PreferenceDialogFragmentCompat}.
     */
    public static RestorePreferenceDialogFragment newInstance(@NonNull final String key) {
        final RestorePreferenceDialogFragment f = new RestorePreferenceDialogFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_KEY, key);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sd = SettingsDatabase.getInstance(getActivity().getApplication());
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            final String message = sd.restoreDatabase();

            if (message.equals("success")) {
                Toast.makeText(getActivity(), R.string.preference_restore_success,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

        super.onClick(dialog, which);
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        // Nothing to do here.
    }
}
