/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.dagger

import android.content.Context
import android.location.LocationManager
import android.os.Build
import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.location.AndroidLocationSupport
import uk.org.rivernile.android.bustracker.core.location.HasLocationFeatureDetector
import uk.org.rivernile.android.bustracker.core.location.IsLocationEnabledDetector
import uk.org.rivernile.android.bustracker.core.location.IsLocationEnabledFetcher
import uk.org.rivernile.android.bustracker.core.location.LegacyIsLocationEnabledFetcher
import uk.org.rivernile.android.bustracker.core.location.V28IsLocationEnabledFetcher
import javax.inject.Provider

/**
 * This Dagger [Module] provides dependencies for all things location.
 *
 * @author Niall Scott
 */
@Module(includes = [ LocationModule.Bindings::class ])
internal class LocationModule {

    @Provides
    fun provideIsLocationEnabledFetcher(
            locationManagerProvider: Provider<LocationManager>,
            contextProvider: Provider<Context>,
            @ForDefaultDispatcher defaultDispatcherProvider: Provider<CoroutineDispatcher>)
            : IsLocationEnabledFetcher {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            V28IsLocationEnabledFetcher(locationManagerProvider.get())
        } else {
            LegacyIsLocationEnabledFetcher(
                    contextProvider.get(),
                    defaultDispatcherProvider.get())
        }
    }

    @Module
    interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindHasLocationFeatureDetector(
                androidLocationSupport: AndroidLocationSupport): HasLocationFeatureDetector

        @Suppress("unused")
        @Binds
        fun bindIsLocationEnabledDetector(
                androidLocationSupport: AndroidLocationSupport): IsLocationEnabledDetector
    }
}