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

package uk.org.rivernile.android.bustracker.core.services

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.ServicesDao
import uk.org.rivernile.android.bustracker.core.di.ForIoDispatcher
import javax.inject.Inject

/**
 * This repository is used to access services data.
 *
 * @param servicesDao The DAO to access the services data store.
 * @param serviceColourOverride An implementation which may override the loaded service colours with
 * a hard-wired implementation. The actual implementation will most likely be defined per product
 * flavour.
 * @param ioDispatcher The [CoroutineDispatcher] to perform IO operations on.
 * @author Niall Scott
 */
class ServicesRepository @Inject internal constructor(
        private val servicesDao: ServicesDao,
        private val serviceColourOverride: ServiceColourOverride?,
        @ForIoDispatcher private val ioDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] which returns a [Map] of service names to colours for the service, and will emit
     * further items if the backing store changes.
     *
     * @param services The services to get colours for. If `null` or empty, colours for all known
     * services will be returned.
     * @return The [Flow] which emits the service-colour mapping.
     */
    @ExperimentalCoroutinesApi
    fun getColoursForServicesFlow(services: Array<String>?)
            : Flow<Map<String, Int>?> = callbackFlow {
        val listener = object : ServicesDao.OnServicesChangedListener {
            override fun onServicesChanged() {
                launch {
                    getAndSendColoursForServices(channel, services)
                }
            }
        }

        servicesDao.addOnServicesChangedListener(listener)
        getAndSendColoursForServices(channel, services)

        awaitClose {
            servicesDao.removeOnServicesChangedListener(listener)
        }
    }

    /**
     * A suspended function which obtains the colours for services and then sends it to the given
     * [channel].
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     * @param services The services to get colours for. If `null`, gets colours for all known
     * services.
     */
    private suspend fun getAndSendColoursForServices(
            channel: SendChannel<Map<String, Int>?>,
            services: Array<String>?) = withContext(ioDispatcher) {
        val serviceColoursFromDao = servicesDao.getColoursForServices(services)
        val serviceColours = if (serviceColourOverride != null) {
            serviceColourOverride.overrideServiceColours(services, serviceColoursFromDao)
        } else {
            serviceColoursFromDao
        }

        channel.send(serviceColours)
    }
}