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

package uk.org.rivernile.android.bustracker.endpoints;

import android.content.Context;
import uk.org.rivernile.android.bustracker.parser.livetimes.BusParser;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.fetchers.HttpFetcher;

/**
 * This class defines an endpoint for accessing the bus tracker API via HTTP.
 * 
 * @author Niall Scott
 */
public class HttpBusTrackerEndpoint extends BusTrackerEndpoint {
    
    private final Context context;
    private final UrlBuilder urlBuilder;
    
    /**
     * Create a new endpoint.
     * 
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @param parser The parser to use to parse the data that comes from the
     * source. Must not be {@code null}.
     * @param urlBuilder A {@link UrlBuilder} instance, used to construct URLs
     * for contacting remote resources. Must not be {@code null}.
     */
    public HttpBusTrackerEndpoint(final Context context, final BusParser parser,
            final UrlBuilder urlBuilder) {
        super(parser);
        
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        if (urlBuilder == null) {
            throw new IllegalArgumentException("urlBuilder must not be null.");
        }
        
        this.context = context;
        this.urlBuilder = urlBuilder;
    }

    @Override
    public LiveBusTimes getBusTimes(final String[] stopCodes,
            final int numDepartures) throws LiveTimesException {
        final HttpFetcher fetcher = new HttpFetcher(context, urlBuilder
                .getBusTimesUrl(stopCodes, numDepartures).toString(), false);
        
        return getParser().getBusTimes(fetcher);
    }

    @Override
    public Journey getJourneyTimes(final String stopCode,
            final String journeyId) throws LiveTimesException {
        final HttpFetcher fetcher = new HttpFetcher(context, urlBuilder
                .getJourneyTimesUrl(stopCode, journeyId).toString(), false);
        
        return getParser().getJourneyTimes(fetcher);
    }
}