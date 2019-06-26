/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.core.database.busstop

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.DatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiException
import uk.org.rivernile.android.bustracker.core.endpoints.api.ApiRequest
import uk.org.rivernile.android.bustracker.core.endpoints.api.DatabaseVersion

/**
 * Unit tests for [DatabaseUpdateCheckerSession].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class DatabaseUpdateCheckerSessionTest {

    @Mock
    lateinit var apiEndpoint: ApiEndpoint
    @Mock
    lateinit var databaseInformationDao: DatabaseInformationDao
    @Mock
    lateinit var databaseUpdater: DatabaseUpdater

    @Mock
    lateinit var apiRequest: ApiRequest<DatabaseVersion>
    @Mock
    lateinit var updateSession: DatabaseUpdaterSession

    private lateinit var session: DatabaseUpdateCheckerSession

    @Before
    fun setUp() {
        session = DatabaseUpdateCheckerSession(apiEndpoint, databaseInformationDao,
                databaseUpdater)

        whenever(apiEndpoint.createDatabaseVersionRequest())
                .thenReturn(apiRequest)
        whenever(databaseUpdater.createNewSession(any()))
                .thenReturn(updateSession)
    }

    @Test
    fun returnFalseWhenPerformingTheApiRequestFails() {
        whenever(apiRequest.performRequest())
                .thenThrow(ApiException::class.java)

        val result = session.checkForDatabaseUpdates()

        assertFalse(result)
    }

    @Test
    fun returnTrueButDontUpdateDatabaseWhenTopologyIdsAreSame() {
        val databaseVersion = createDatabaseVersion("abc123")
        whenever(apiRequest.performRequest())
                .thenReturn(databaseVersion)
        whenever(databaseInformationDao.getTopologyId())
                .thenReturn("abc123")

        val result = session.checkForDatabaseUpdates()

        assertTrue(result)
        verify(updateSession, never())
                .updateDatabase()
    }

    @Test
    fun returnTrueWhenTopologyIdsAreDifferentAndDatabaseUpdateIsSuccessful() {
        val databaseVersion = createDatabaseVersion("abc123")
        whenever(apiRequest.performRequest())
                .thenReturn(databaseVersion)
        whenever(databaseInformationDao.getTopologyId())
                .thenReturn("xyz789")
        whenever(updateSession.updateDatabase())
                .thenReturn(true)

        val result = session.checkForDatabaseUpdates()

        assertTrue(result)
        verify(updateSession)
                .updateDatabase()
    }

    @Test
    fun returnFalseWhenTopologyIdsAreDifferentAndDatabaseUpdateFails() {
        val databaseVersion = createDatabaseVersion("abc123")
        whenever(apiRequest.performRequest())
                .thenReturn(databaseVersion)
        whenever(databaseInformationDao.getTopologyId())
                .thenReturn("xyz789")
        whenever(updateSession.updateDatabase())
                .thenReturn(false)

        val result = session.checkForDatabaseUpdates()

        assertFalse(result)
        verify(updateSession)
                .updateDatabase()
    }

    @Test(expected = IllegalStateException::class)
    fun throwsIllegalStateExceptionWhenSessionIsReRun() {
        val databaseVersion = createDatabaseVersion("abc123")
        whenever(apiRequest.performRequest())
                .thenReturn(databaseVersion)
        whenever(databaseInformationDao.getTopologyId())
                .thenReturn("xyz789")
        whenever(updateSession.updateDatabase())
                .thenReturn(true)

        session.checkForDatabaseUpdates()
        session.checkForDatabaseUpdates()
    }

    @Test
    fun cancellingTheSessionCancelsTheApiRequest() {
        val databaseVersion = createDatabaseVersion("abc123")
        whenever(apiRequest.performRequest())
                .thenReturn(databaseVersion)
        whenever(databaseInformationDao.getTopologyId())
                .thenReturn("xyz789")
        whenever(updateSession.updateDatabase())
                .thenReturn(true)

        session.checkForDatabaseUpdates()
        session.cancel()

        verify(apiRequest)
                .cancel()
    }

    private fun createDatabaseVersion(topologyId: String) =
            DatabaseVersion("MBE", topologyId, "http://host/db.db", "abcdef1234567890")
}