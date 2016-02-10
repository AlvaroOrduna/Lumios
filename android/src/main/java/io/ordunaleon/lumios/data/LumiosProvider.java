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

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import io.ordunaleon.lumios.data.LumiosContract.PriceEntry;
import io.ordunaleon.lumios.utils.LogUtils;

public class LumiosProvider extends ContentProvider {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private LumiosDbHelper mOpenHelper;

    static final int PRICE = 100;
    static final int PRICE_WITH_INDICATOR_ID = 101;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = LumiosContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, LumiosContract.PATH_PRICE, PRICE);
        matcher.addURI(authority, LumiosContract.PATH_PRICE + "/*", PRICE_WITH_INDICATOR_ID);

        return matcher;
    }

    // date >= ?
    private static final String sStartDateSelection = PriceEntry.COLUMN_DATE + " >= ? ";

    // date <= ?
    private static final String sEndDatetSelection = PriceEntry.COLUMN_DATE + " <= ? ";

    // date = ?
    private static final String sDatetSelection = PriceEntry.COLUMN_DATE + " = ? ";

    // date >= ? AND date <= ?
    private static final String sStartDateAndEndDateSelection =
            PriceEntry.COLUMN_DATE + " >= ? AND " + PriceEntry.COLUMN_DATE + " <= ? ";

    private Cursor getPrice(Uri uri, String[] projection, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        String date = PriceEntry.getDateFromUri(uri);
        String startDate = PriceEntry.getStartDateFromUri(uri);
        String endDate = PriceEntry.getEndDateFromUri(uri);

        String selection = null;
        String[] selectionArgs = null;

        if (date != null && (startDate != null || endDate != null)) {
            LogUtils.LOGE(LOG_TAG, "Incorrect SQL query statement: ((date >= ? OR date <= ?) AND date = ?)");
        } else if (startDate != null && endDate != null) {
            // date >= ? AND date <= ?
            selection = sStartDateAndEndDateSelection;
            selectionArgs = new String[]{startDate, endDate};
        } else if (date != null) {
            // date = ?
            selection = sDatetSelection;
            selectionArgs = new String[]{date};
        } else if (startDate != null) {
            // date >= ?
            selection = sStartDateSelection;
            selectionArgs = new String[]{startDate};
        } else if (endDate != null) {
            // date <= ?
            selection = sEndDatetSelection;
            selectionArgs = new String[]{endDate};
        }

        return db.query(
                PriceEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new LumiosDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case PRICE:
                return PriceEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final int match = sUriMatcher.match(uri);

        Cursor retCursor;

        switch (match) {
            case PRICE:
                retCursor = getPrice(uri, selectionArgs, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown query uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        Uri returnUri;

        switch (match) {
            case PRICE:
                long _id = db.insertOrThrow(PriceEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PriceEntry.buildUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsUpdated;

        switch (match) {
            case PRICE:
                rowsUpdated = db.update(PriceEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        int rowsDeleted;

        switch (match) {
            case PRICE: {
                rowsDeleted = db.delete(PriceEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }


    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRICE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(PriceEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
