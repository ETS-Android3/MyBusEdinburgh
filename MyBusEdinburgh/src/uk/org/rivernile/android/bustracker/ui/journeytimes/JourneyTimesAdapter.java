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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Adapter} populates the rows which show the arrival time for a bus
 * at each of its stops on a journey.
 * 
 * @author Niall Scott
 */
public class JourneyTimesAdapter extends BaseAdapter {
    
    private final Context context;
    private final LayoutInflater inflater;
    private Journey journey;
    private List<JourneyDeparture> departures;
    
    /**
     * Create a new {@code JourneyTimesAdapter} which provides the layouts for
     * the rows to display journey times.
     * 
     * @param context A {@link Context} instance. Must not be {@code null}.
     */
    public JourneyTimesAdapter(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return departures != null ? departures.size() : 0;
    }

    @Override
    public JourneyDeparture getItem(final int position) {
        return position >= 0 && position < getCount() ?
                departures.get(position) : null;
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).getStopCode().hashCode();
    }

    @Override
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        final JourneyDeparture departure = getItem(position);
        if (departure == null) {
            return null;
        }
        
        final View v;
        final ViewHolder holder;
        if (convertView == null) {
            v = inflater.inflate(R.layout.journeytimes_row, parent, false);
            
            holder = new ViewHolder();
            holder.txtStopName = (TextView) v.findViewById(R.id.txtStopName);
            holder.txtBusTime = (TextView) v.findViewById(R.id.txtBusTime);
            
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (ViewHolder) v.getTag();
        }
        
        holder.txtStopName.setText(departure.getStopName() + " "
                + departure.getStopCode());
        
        final int departureMinutes = departure.getDepartureMinutes();
        holder.txtBusTime.setText(String.valueOf(departureMinutes));
        
        final boolean enabled = departureMinutes >= 0;
        holder.txtStopName.setEnabled(enabled);
        holder.txtBusTime.setEnabled(enabled);
        
        return v;
    }
    
    /**
     * Get the {@link Context} used in this {@link Adapter}.
     * 
     * @return The {@link Context} used in this {@link Adapter}.
     */
    public final Context getContext() {
        return context;
    }
    
    /**
     * Set the journey to display.
     * 
     * @param journey The journey to display. Can be {@code null} if nothing is
     * being displayed.
     */
    public void setJourney(final Journey journey) {
        if (this.journey != journey) {
            this.journey = journey;
            departures = journey.getDepartures();
            notifyDataSetChanged();
        }
    }
    
    /**
     * Get the journey being displayed.
     * 
     * @return The journey being displayed. Will be {@code null} if there's no
     * journey being displayed.
     */
    public Journey getJourney() {
        return journey;
    }
    
    /**
     * This class holds references to the {@link View} objects so that they
     * don't need to be found later, as {@link View} finding can be expensive.
     */
    protected static class ViewHolder {
        TextView txtStopName;
        TextView txtBusTime;
    }
}