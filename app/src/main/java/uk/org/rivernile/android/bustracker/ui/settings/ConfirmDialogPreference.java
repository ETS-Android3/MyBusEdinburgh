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

import android.content.Context;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

/**
 * Define a {@link DialogPreference} that can be used to launch a
 * {@link android.support.v7.preference.PreferenceDialogFragmentCompat} to ask the user for
 * confirmation.
 *
 * @author Niall Scott
 */
public class ConfirmDialogPreference extends DialogPreference {

    public ConfirmDialogPreference(final Context context, final AttributeSet attrs,
            final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        setPersistent(false);
    }

    public ConfirmDialogPreference(final Context context, final AttributeSet attrs,
            final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setPersistent(false);
    }

    public ConfirmDialogPreference(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        setPersistent(false);
    }

    public ConfirmDialogPreference(final Context context) {
        super(context);

        setPersistent(false);
    }
}
