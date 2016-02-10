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

package io.ordunaleon.lumios.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class LumiosContract {

    private static final String URI_QUERY_PARAM_DATE = "date";
    private static final String URI_QUERY_PARAM_START_DATE = "start_date";
    private static final String URI_QUERY_PARAM_END_DATE = "end_date";

    interface PriceColumns {
        String COLUMN_DATE = "date";
        String COLUMN_PRICE_GENERAL = "price_general";
        String COLUMN_AVG_GENERAL = "avg_general";
        String COLUMN_PRICE_NIGHT = "price_night";
        String COLUMN_AVG_NIGHT = "avg_night";
        String COLUMN_PRICE_VEHICLE = "price_vehicle";
        String COLUMN_AVG_VEHICLE = "avg_vehicle";
    }

    public static final String CONTENT_AUTHORITY = "io.ordunaleon.lumios";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_PRICE = "price";

    /* Inner class that defines the table contents of the price table */
    public static final class PriceEntry implements PriceColumns, BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_PRICE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRICE;

        public static final String TABLE_NAME = "price";

        public static Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUriWithDate(String date) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(URI_QUERY_PARAM_DATE, date)
                    .build();
        }

        public static Uri buildUriWithStartDate(String startDate) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(URI_QUERY_PARAM_START_DATE, startDate)
                    .build();
        }

        public static Uri buildUriWithEndDate(String endDate) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(URI_QUERY_PARAM_END_DATE, endDate)
                    .build();
        }

        public static Uri buildUriWithStartDateAndEndDate(String startDate, String endDate) {
            return CONTENT_URI.buildUpon()
                    .appendQueryParameter(URI_QUERY_PARAM_START_DATE, startDate)
                    .appendQueryParameter(URI_QUERY_PARAM_END_DATE, endDate)
                    .build();
        }

        public static String getDateFromUri(Uri uri) {
            return uri.getQueryParameter(URI_QUERY_PARAM_DATE);
        }

        public static String getStartDateFromUri(Uri uri) {
            return uri.getQueryParameter(URI_QUERY_PARAM_START_DATE);
        }

        public static String getEndDateFromUri(Uri uri) {
            return uri.getQueryParameter(URI_QUERY_PARAM_END_DATE);
        }
    }
}
