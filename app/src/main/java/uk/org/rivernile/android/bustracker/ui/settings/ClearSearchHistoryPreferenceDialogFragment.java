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
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

import uk.org.rivernile.edinburghbustracker.android.MapSearchSuggestionsProvider;

/**
 * Shows a {@link PreferenceDialogFragmentCompat} asking the user to confirm if they wish to
 * clear their search history. If the user confirms, then their search history is cleared.
 *
 * @author Niall Scott
 */
public class ClearSearchHistoryPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private SearchRecentSuggestions suggestions;

    /**
     * Create a new instance of this {@link PreferenceDialogFragmentCompat}.
     *
     * @param key The key of the {@link android.support.v7.preference.Preference}.
     * @return A new instance of this {@link PreferenceDialogFragmentCompat}.
     */
    public static ClearSearchHistoryPreferenceDialogFragment newInstance(
            @NonNull final String key) {
        final ClearSearchHistoryPreferenceDialogFragment f =
                new ClearSearchHistoryPreferenceDialogFragment();
        final Bundle b = new Bundle();
        b.putString(ARG_KEY, key);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        suggestions = new SearchRecentSuggestions(getActivity(),
                MapSearchSuggestionsProvider.AUTHORITY,
                MapSearchSuggestionsProvider.MODE);
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            suggestions.clearHistory();
        }

        super.onClick(dialog, which);
    }

    @Override
    public void onDialogClosed(final boolean positiveResult) {
        // Nothing to do here.
    }
}
