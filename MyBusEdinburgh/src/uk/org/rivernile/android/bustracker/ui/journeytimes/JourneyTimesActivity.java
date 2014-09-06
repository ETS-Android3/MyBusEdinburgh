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

package uk.org.rivernile.android.bustracker.ui.journeytimes;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.edinburghbustracker.android.BusStopDetailsActivity;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Activity} hosts a {@link JourneyTimesFragment} which displays
 * journey times for a given journey ID beginning at a given bus stop code.
 * 
 * @author Niall Scott
 */
public class JourneyTimesActivity extends ActionBarActivity
        implements JourneyTimesFragment.Callbacks {
    
    /** The {@link Intent} argument for the {@code stopCode}. */
    public static final String ARG_STOPCODE = JourneyTimesFragment.ARG_STOPCODE;
    /** The {@link Intent} argument for the journey ID. */
    public static final String ARG_JOURNEY_ID =
            JourneyTimesFragment.ARG_JOURNEY_ID;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.single_fragment_container);
        
        if (savedInstanceState == null) {
            final Intent intent = getIntent();
            final BusApplication app = (BusApplication) getApplication();
            final JourneyTimesFragment f = app.getFragmentFactory()
                    .getJourneyTimesFragment(
                            intent.getStringExtra(ARG_STOPCODE),
                            intent.getStringExtra(ARG_JOURNEY_ID));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragmentContainer, f)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // TODO: handle up navigation
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onShowBusStopDetails(final String stopCode) {
        final Intent intent = new Intent(this, BusStopDetailsActivity.class);
        intent.putExtra(BusStopDetailsActivity.ARG_STOPCODE, stopCode);
        
        startActivity(intent);
    }
}