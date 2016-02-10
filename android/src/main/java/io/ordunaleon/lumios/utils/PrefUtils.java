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

import io.ordunaleon.lumios.R;

public class PrefUtils {

    /**
     * Boolean indicating whether the app has performed the (one-time) welcome flow.
     */
    public static final String PREF_WELCOME_DONE = "pref_welcome_done";

    /**
     * Integer indicating which of the three available fares is selected.
     */
    public static final int PREF_FARE_KEY = R.string.pref_fare_key;
    public static final int PREF_FARE_DEFAULT = R.string.pref_fare_default_value;

    /**
     * Boolean indicating whether the app has been registered with GCM.
     */
    public static final int PREF_SENT_TOKEN_KEY = R.string.pref_sent_token_key;

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
        final String key = context.getResources().getString(PREF_FARE_KEY);
        final String defValue = context.getResources().getString(PREF_FARE_DEFAULT);
        return Integer.parseInt(sp.getString(key, defValue));
    }

    /**
     * Return the name of the selected fare.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static String getFareName(final Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        final String key = context.getResources().getString(PREF_FARE_KEY);
        final String defValue = context.getResources().getString(PREF_FARE_DEFAULT);
        int fareIndex = Integer.parseInt(sp.getString(key, defValue));
        String[] fareEntries = context.getResources().getStringArray(R.array.fare_entries);
        return fareEntries[fareIndex];
    }

    /**
     * Set the index of the selected fare.
     *
     * @param context Context to be used to lookup the {@link SharedPreferences}.
     */
    public static void setFareIndex(final Context context, final int value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putString(context.getResources().getString(PREF_FARE_KEY),
                String.valueOf(value)).apply();
    }

    /**
     * Store a boolean indicating whether the app has been registered with GCM.
     *
     * @param context The context of the preferences where values are stored.
     * @param value   The value to store.
     */
    public static void setSentToken(Context context, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean(context.getResources().getString(PREF_SENT_TOKEN_KEY), value).apply();
    }

    /**
     * Get the boolean indicating whether the app has been registered with GCM.
     *
     * @param context The context of the preferences where values are stored.
     * @return Boolean The value stored.
     */
    public static boolean getSentToken(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean(context.getResources().getString(PREF_SENT_TOKEN_KEY), false);
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
