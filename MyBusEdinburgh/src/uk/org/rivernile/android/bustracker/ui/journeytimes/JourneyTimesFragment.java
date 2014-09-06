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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.net.UnknownHostException;
import org.json.JSONException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyTimesLoader;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes.ServerErrorException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .SystemOverloadedException;
import uk.org.rivernile.android.bustracker.ui.callbacks
        .OnShowBusStopDetailsListener;
import uk.org.rivernile.android.fetchers.ConnectivityUnavailableException;
import uk.org.rivernile.android.fetchers.UrlMismatchException;
import uk.org.rivernile.android.utils.LoaderResult;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The {@code JourneyTimesFragment} displays journey times for a given journey
 * ID starting at a given bus stop code as a list.
 * 
 * @author Niall Scott
 */
public class JourneyTimesFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<
                LoaderResult<Journey, LiveTimesException>>{
    
    /** This is the stopCode argument. */
    public static final String ARG_STOPCODE = "stopCode";
    /** This is the journeyId argument. */
    public static final String ARG_JOURNEY_ID = "journeyId";
    
    private static final String KEY_LAST_REFRESH = "lastRefresh";
    
    private Callbacks callbacks;
    private JourneyTimesAdapter adapter;
    private String stopCode;
    private String journeyId;
    private long lastRefresh = 0;
    private boolean journeyTimesLoading = false;
    
    private MenuItem refreshMenuItem;
    
    private View rootView;
    private ProgressBar progress;
    private TextView txtError;
    
    /**
     * Create a new instance of this {@link Fragment}, specifying the
     * {@code stopCode} and the {@code journeyId}.
     * 
     * @param stopCode The stop code of the departure bus stop on the journey.
     * @param journeyId The unique ID of the journey.
     * @return A new instance of this {@link Fragment}.
     */
    public static JourneyTimesFragment newInstance(final String stopCode,
            final String journeyId) {
        final JourneyTimesFragment f = new JourneyTimesFragment();
        final Bundle args = new Bundle();
        args.putString(ARG_STOPCODE, stopCode);
        args.putString(ARG_JOURNEY_ID, journeyId);
        f.setArguments(args);
        
        return f;
    }

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        final Bundle args = getArguments();
        stopCode = args.getString(ARG_STOPCODE);
        journeyId = args.getString(ARG_JOURNEY_ID);
        
        if (savedInstanceState != null) {
            lastRefresh = savedInstanceState.getLong(KEY_LAST_REFRESH, 0);
        }
        
        adapter = createAdapter();
        setListAdapter(adapter);
    }
    
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.journeytimes, container, false);
        progress = (ProgressBar) rootView.findViewById(R.id.progress);
        txtError = (TextView) rootView.findViewById(R.id.txtError);
        
        return rootView;
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        if (TextUtils.isEmpty(stopCode)) {
            showError(getString(R.string.journeytimes_err_nocode));
        } else if (TextUtils.isEmpty(journeyId)) {
            showError(getString(R.string.journeytimes_err_nojourneyid));
        } else {
            loadJourneyTimes(false);
            setHasOptionsMenu(true);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putLong(KEY_LAST_REFRESH, lastRefresh);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        inflater.inflate(R.menu.journeytimes_option_menu, menu);
        
        refreshMenuItem = menu.findItem(R.id.journeytimes_option_menu_refresh);
    }

    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        configureRefreshActionItem();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.journeytimes_option_menu_refresh:
                doRefreshSelected();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<LoaderResult<Journey, LiveTimesException>> onCreateLoader(
            final int i, final Bundle bundle) {
        return new JourneyTimesLoader(getActivity(), stopCode, journeyId);
    }

    @Override
    public void onLoadFinished(
            final Loader<LoaderResult<Journey, LiveTimesException>> loader,
            final LoaderResult<Journey, LiveTimesException> result) {
        journeyTimesLoading = false;
        
        if (result != null) {
           lastRefresh = result.getLoadTime();
           
           rootView.post(new Runnable() {
               @Override
               public void run() {
                   if (result.hasException()) {
                       handleError(result.getException());
                   } else {
                       displayJourneyTimes(result.getResult());
                   }
               }
           });
        }
    }

    @Override
    public void onLoaderReset(
            final Loader<LoaderResult<Journey, LiveTimesException>> loader) {
        // Nothing to do here.
    }

    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        final JourneyDeparture departure = adapter.getItem(position);
        
        if (departure != null) {
            callbacks.onShowBusStopDetails(departure.getStopCode());
        }
    }
    
    /**
     * This method returns an appropriate {@link JourneyTimesAdapter} to use in
     * this {@code JourneyTimesFragment}. Subclasses may want to override this
     * method to return a subclassed {@link JourneyTimesAdapter}.
     * 
     * @return The {@link JourneyTimesAdapter} to use in this
     * {@link JourneyTimesFragment}.
     */
    protected JourneyTimesAdapter createAdapter() {
        return new JourneyTimesAdapter(getActivity());
    }
    
    /**
     * Start loading journey times.
     * 
     * @param reload {@code true} if the data should be forced reloaded,
     * {@code false} if not.
     */
    private void loadJourneyTimes(final boolean reload) {
        journeyTimesLoading = true;
        showProgress();
        
        if (reload) {
            getLoaderManager().restartLoader(0, null, this);
        } else {
            getLoaderManager().initLoader(0, null, this);
        }
    }
    
    /**
     * Handle errors.
     * 
     * @param exception The exception from the model.
     */
    private void handleError(final LiveTimesException exception) {
        if (exception == null) {
            showError(getString(R.string.journeytimes_err_unknown));
            return;
        }
        
        final Throwable cause = exception.getCause();
        final Throwable e = cause != null ? cause : exception;
        final String errorMessage;
        
        if (e instanceof ConnectivityUnavailableException) {
            errorMessage = getString(R.string.journeytimes_err_noconn);
        } else if (e instanceof UnknownHostException) {
            errorMessage = getString(R.string.journeytimes_err_noresolv);
        } else if (e instanceof UrlMismatchException) {
            errorMessage = getString(R.string.journeytimes_err_urlmismatch);
        } else if (e instanceof JSONException) {
            errorMessage = getString(R.string.journeytimes_err_parseerr);
        } else if (e instanceof AuthenticationException) {
            errorMessage = getString(R.string.journeytimes_err_api_invalid_key);
        } else if (e instanceof ServerErrorException) {
            errorMessage = getString(R.string
                    .journeytimes_err_api_processing_error);
        } else if (e instanceof MaintenanceException) {
            errorMessage = getString(R.string
                        .journeytimes_err_api_system_maintenance);
        } else if (e instanceof SystemOverloadedException) {
            errorMessage = getString(R.string
                        .journeytimes_err_api_system_overloaded);
        } else {
            errorMessage = getString(R.string.journeytimes_err_unknown);
        }
        
        showError(errorMessage);
    }
    
    /**
     * This is called when the journey times have loaded and should be
     * displayed.
     * @param journey The journey data that has been loaded.
     */
    private void displayJourneyTimes(final Journey journey) {
        adapter.setJourney(journey);
        showJourney();
    }
    
    /**
     * Show progress when journey times are loading.
     */
    private void showProgress() {
        txtError.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        configureRefreshActionItem();
    }
    
    /**
     * Show the journey data.
     */
    private void showJourney() {
        progress.setVisibility(View.GONE);
        txtError.setVisibility(View.GONE);
        configureRefreshActionItem();
    }
    
    /**
     * Show an error.
     * 
     * @param errorMessage The error message to display to the user.
     */
    private void showError(final String errorMessage) {
        progress.setVisibility(View.GONE);
        adapter.setJourney(null);
        txtError.setText(errorMessage);
        txtError.setVisibility(View.VISIBLE);
        
        configureRefreshActionItem();
    }
    
    /**
     * Configure the refresh menu item with the correct state.
     */
    private void configureRefreshActionItem() {
        if (refreshMenuItem != null) {
            if (journeyTimesLoading) {
                refreshMenuItem.setEnabled(false);
                MenuItemCompat.setActionView(refreshMenuItem,
                        R.layout.actionbar_indeterminate_progress);
            } else {
                refreshMenuItem.setEnabled(true);
                MenuItemCompat.setActionView(refreshMenuItem, null);
            }
        }
    }
    
    /**
     * This is called when the refresh ActionItem is selected.
     */
    private void doRefreshSelected() {
        loadJourneyTimes(true);
    }
    
    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must
     * implement this interface to handle navigation events.
     */
    public static interface Callbacks extends OnShowBusStopDetailsListener {
        
        // All required interfaces have been defined in other interfaces.
    }
}