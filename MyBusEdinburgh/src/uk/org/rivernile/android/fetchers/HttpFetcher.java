/*
 * Copyright (C) 2013 - 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.fetchers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * A {@code HttpFetcher} fetches data from a HTTP server, specified by the given
 * URL. The data is then given in to an instance of a
 * {@link FetcherStreamReader}. This class takes care of opening and closing the
 * file.
 * 
 * @author Niall Scott
 */
public class HttpFetcher implements Fetcher {
    
    private final ConnectivityManager connMan;
    private final String url;
    private final boolean allowRedirects;
    
    /**
     * Create a new {@code HttpFetcher}.
     * 
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @param url The URL to fetch from. Must not be {@code null} or empty.
     * @param allowRedirects {@code true} if redirects are allowed,
     * {@code false} if not.
     */
    public HttpFetcher(final Context context, final String url,
            final boolean allowRedirects) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url must not be null or "
                    + "empty.");
        }
        
        connMan = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        this.url = url;
        this.allowRedirects = allowRedirects;
    }

    @Override
    public void executeFetcher(final FetcherStreamReader reader)
            throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("The reader cannot be null.");
        }
        
        // Check that network access is possible before continuing.
        final NetworkInfo networkInfo = connMan.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new ConnectivityUnavailableException();
        }
        
        HttpURLConnection conn = null;
        
        try {
            final URL u = new URL(url);
            conn = (HttpURLConnection) u.openConnection();
            final InputStream in = conn.getInputStream();
            
            if (!allowRedirects &&
                    !u.getHost().equals(conn.getURL().getHost())) {
                conn.disconnect();
                throw new UrlMismatchException();
            }
            
            reader.readInputStream(in);
        } catch (IOException e) {
            // Re-throw the Exception, so that the 'finally' clause is
            // executed.
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
    
    /**
     * Get the URL used by this {@code HttpFetcher}.
     * 
     * @return The URL used by this {@code HttpFetcher}.
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * Get whether this implementation allows redirects or not.
     * 
     * @return {@code true} if redirects are allowed, {@code false} if not.
     */
    public boolean getAllowRedirects() {
        return allowRedirects;
    }
}