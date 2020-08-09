/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.livetimes

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import javax.inject.Inject

/**
 * This repository is used to access live times.
 *
 * @param trackerEndpoint The endpoint to receive [LiveTimes] from.
 * @param ioDispatcher The [CoroutineDispatcher] to perform IO operations on.
 * @author Niall Scott
 */
class LiveTimesRepository @Inject internal constructor(
        private val trackerEndpoint: TrackerEndpoint,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] object which contains the [Result] of loading [LiveTimes].
     *
     * @param stopCode The stop code to load [LiveTimes] for.
     * @param numberOfDepartures The number of departures per services to obtain.
     * @return A [Flow] object containing the [Result] of loading [LiveTimes].
     */
    fun getLiveTimesFlow(stopCode: String, numberOfDepartures: Int)
            : Flow<Result<LiveTimes>> = flow {
        emit(Result.InProgress)
        emit(fetchLiveTimes(stopCode, numberOfDepartures))
    }

    /**
     * This suspending function, executed on the IO [CoroutineDispatcher], fetches the live times
     * for the given stop code and returns the appropriate [Result] object.
     *
     * @param stopCode The stop code to load [LiveTimes] for.
     * @param numberOfDepartures The number of departures per services to obtain.
     * @return A [Result] object encapsulating the result of the request.
     */
    private suspend fun fetchLiveTimes(
            stopCode: String,
            numberOfDepartures: Int): Result<LiveTimes> = withContext(ioDispatcher) {
        val request = trackerEndpoint.createLiveTimesRequest(stopCode, numberOfDepartures)

        try {
            Result.Success(request.performRequest())
        } catch (e: TrackerException) {
            Result.Error(e)
        } catch (e: CancellationException) {
            request.cancel()
            throw e
        }
    }
}