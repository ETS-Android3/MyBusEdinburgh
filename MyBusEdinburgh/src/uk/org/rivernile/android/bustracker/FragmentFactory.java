/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker;

import android.support.v4.app.Fragment;
import uk.org.rivernile.android.bustracker.ui.bustimes
        .DisplayStopDataFragment;
import uk.org.rivernile.android.bustracker.ui.journeytimes.JourneyTimesFragment;

/**
 * The {@code FragmentFactory} returns the correct {@link Fragment} for a
 * particular {@link Fragment} type, depending on the city implementation of the
 * application.
 * 
 * @author Niall Scott
 */
public interface FragmentFactory {
    
    /**
     * Get the correct instance of {@link DisplayStopDataFragment} for the
     * current city.
     * 
     * @param stopCode The {@code stopCode} to show stop data for.
     * @return An instance of the {@link DisplayStopDataFragment} for a
     * particular city.
     */
    public DisplayStopDataFragment getDisplayStopDataFragment(String stopCode);
    
    /**
     * Get the correct instance of {@link JourneyTimesFragment} for the current
     * city.
     * 
     * @param stopCode The {@code stopCode} of the starting stop.
     * @param journeyId The unique journey ID.
     * @return An instance of the {@link JourneyTimesFragment} for a particular
     * city.
     */
    public JourneyTimesFragment getJourneyTimesFragment(String stopCode,
            String journeyId);
}