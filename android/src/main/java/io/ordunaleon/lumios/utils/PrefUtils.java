/*
 * Copyright (C) 2016 Álvaro Orduna León
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.ordunaleon.lumios.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;

public class PrefUtils {

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /**
     * Integer indicating which of the three available fares is selected.
     */
    public static final String PREF_FARE_KEY = "pref_fare";

    /**
     * Return true if the first app run have already been executed.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static boolean isWelcomeDone(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(PREF_WELCOME_DONE, false);
    }

    /**
     * Mark {@code newValue whether} this is the first time the first app run is done.
     *
     * @param context  Context to be used to lookup the {@link SharedPreferences}.
     * @param newValue New value that will be set.
     */
    public static void setWelcomeDone(final Context context, final boolean newValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(PREF_WELCOME_DONE, newValue).apply();
    }

    /**
     * Return the index of the selected fare.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static int getFareIndex(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getInt(PREF_FARE_KEY, 0);
    }

    /**
     * Set the index of the selected fare.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static void setFareIndex(final Context context, final int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putInt(PREF_FARE_KEY, value).apply();
    }

    /**
     * Registers a callback to be invoked when a change happens to a preference.
     *
     * @param context  Context in which to register the listener.
     * @param listener The callback that will run.
     * @see #unregisterOnSharedPreferenceChangeListener
     */
    public static void registerOnSharedPreferenceChangeListener(final Context context, OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a previous registered callback.
     *
     * @param context  Context in which to unregister the listener.
     * @param listener The callback that should be unregistered.
     * @see #registerOnSharedPreferenceChangeListener
     */
    public static void unregisterOnSharedPreferenceChangeListener(final Context context, OnSharedPreferenceChangeListener listener) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
