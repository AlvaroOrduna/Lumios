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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.ordunaleon.lumios.data.LumiosContract.PriceEntry;

public class LumiosDbHelper extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "lumios.db";

    private static final int DATABASE_VERSION = 1;

    private static final String SQL_CREATE_PRICE_TABLE =
            "CREATE TABLE " + PriceEntry.TABLE_NAME + " (" +
                    PriceEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    PriceEntry.COLUMN_DATE + " TEXT NOT NULL," +
                    PriceEntry.COLUMN_PRICE_GENERAL + " DOUBLE NOT NULL," +
                    PriceEntry.COLUMN_AVG_GENERAL + " DOUBLE NOT NULL," +
                    PriceEntry.COLUMN_PRICE_NIGHT + " DOUBLE NOT NULL," +
                    PriceEntry.COLUMN_AVG_NIGHT + " DOUBLE NOT NULL," +
                    PriceEntry.COLUMN_PRICE_VEHICLE + " DOUBLE NOT NULL," +
                    PriceEntry.COLUMN_AVG_VEHICLE + " DOUBLE NOT NULL," +

                    // To assure the application have just one price entry per date,
                    // we create a UNIQUE constraint with REPLACE strategy.
                    " UNIQUE (" + PriceEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

    public LumiosDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_PRICE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL("DROP TABLE IF EXISTS " + PriceEntry.TABLE_NAME);
        onCreate(db);
    }
}
