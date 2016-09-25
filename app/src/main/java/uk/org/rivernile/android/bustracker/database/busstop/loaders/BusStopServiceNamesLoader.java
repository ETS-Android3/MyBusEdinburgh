/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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
 * 1. This notice may not be removed or altered from any file it appears in.
 *
 * 2. Any modifications made to this software, except those defined in
 *    clause 3 of this agreement, must be released under this license, and
 *    the source code of any modifications must be made available on a
 *    publically accessible (and locateable) website, or sent to the
 *    original author of this software.
 *
 * 3. Software modifications that do not alter the functionality of the
 *    software but are simply adaptations to a specific environment are
 *    exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.database.busstop.loaders;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase;
import uk.org.rivernile.android.utils.ProcessedCursorLoader;

/**
 * This class is used to load the names of all services that are in the bus stop database that stop
 * at a specified bus stop. The returned {@link android.database.Cursor} will only have the
 * {@link BusStopContract.ServiceStops#SERVICE_NAME} column. The rows will be ordered
 * alphanumerically.
 *
 * @author Niall Scott
 */
public class BusStopServiceNamesLoader extends ProcessedCursorLoader<String[]> {

    /**
     * Create a new {@code BusStopServiceNamesLoader}.
     *
     * @param context A {@link Context} instance.
     * @param stopCode The stop code to get services for.
     */
    public BusStopServiceNamesLoader(@NonNull final Context context,
            @NonNull final String stopCode) {
        super(context, BusStopContract.ServiceStops.CONTENT_URI,
                new String[] { BusStopContract.ServiceStops.SERVICE_NAME },
                BusStopContract.ServiceStops.STOP_CODE + " = ?", new String[] { stopCode },
                BusStopDatabase.getServicesSortByCondition(
                        BusStopContract.ServiceStops.SERVICE_NAME));
    }

    @Nullable
    @Override
    public String[] processCursor(@Nullable final Cursor cursor) {
        final String[] services;

        if (cursor != null) {
            final int count = cursor.getCount();
            final int serviceNameColumn = cursor.getColumnIndex(
                    BusStopContract.ServiceStops.SERVICE_NAME);
            services = new String[count];

            for (int i = 0; i < count; i++) {
                if (cursor.moveToPosition(i)) {
                    services[i] = cursor.getString(serviceNameColumn);
                }
            }
        } else {
            services = null;
        }

        return services;
    }
}
