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

package uk.org.rivernile.android.bustracker.database.busstop;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for {@link BusStopDatabase}.
 *
 * @author Niall Scott
 */
public class BusStopDatabaseTests {

    /**
     * Test that a correct {@code SORT BY} condition is generated in
     * {@link BusStopDatabase#getServicesSortByCondition(String)} with the provided column name.
     */
    @Test
    public void testGetServicesSortByCondition() {
        assertEquals("CASE WHEN test GLOB '[^0-9.]*' THEN test ELSE cast(test AS int) END",
                BusStopDatabase.getServicesSortByCondition("test"));
    }

    /**
     * Test that {@link BusStopDatabase#generateInPlaceholders(int)} returns an empty {@link String}
     * when the count is set as {@code 0}.
     */
    @Test
    public void testGenerateInPlaceholdersWithCountAs0() {
        assertEquals("", BusStopDatabase.generateInPlaceholders(0));
    }

    /**
     * Test that {@link BusStopDatabase#generateInPlaceholders(int)} returns a single placeholder
     * when the count is set as {@code 1}.
     */
    @Test
    public void testGenerateInPlaceholdersWithCountAs1() {
        assertEquals("?", BusStopDatabase.generateInPlaceholders(1));
    }

    /**
     * Test that {@link BusStopDatabase#generateInPlaceholders(int)} returns the correct number of
     * placeholders for the {@code count} value sent in.
     */
    @Test
    public void testGeneratePlaceholdersWithCountAsMany() {
        assertEquals("?,?,?,?,?,?,?,?,?,?", BusStopDatabase.generateInPlaceholders(10));
    }
}
