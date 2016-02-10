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

package io.ordunaleon.lumios.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.util.Calendar;

import io.ordunaleon.lumios.R;
import io.ordunaleon.lumios.ui.PriceListFragment;
import io.ordunaleon.lumios.utils.DateUtils;
import io.ordunaleon.lumios.utils.LogUtils;
import io.ordunaleon.lumios.utils.PrefUtils;

import static io.ordunaleon.lumios.utils.LogUtils.LOGE;

public class PriceListAdapter extends CursorAdapter {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    public PriceListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_price_list, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String dateStr = cursor.getString(PriceListFragment.COL_PRICE_DATE);

        String hour = null;
        try {
            hour = DateUtils.getFieldFromIsoDate(dateStr, Calendar.HOUR_OF_DAY);
        } catch (ParseException e) {
            LOGE(LOG_TAG, "error while getting field from ISO 8601 date in string format ", e);
        }

        String fare = PrefUtils.getFareName(context);
        double avg = 0;
        double price = 0;

        if (fare.equals(context.getString(R.string.pref_fare_entries_general))) {
            avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_GENERAL);
            price = cursor.getDouble(PriceListFragment.COL_PRICE_GENERAL);
        } else if (fare.equals(context.getString(R.string.pref_fare_entries_night))) {
            avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_NIGHT);
            price = cursor.getDouble(PriceListFragment.COL_PRICE_NIGHT);
        } else if (fare.equals(context.getString(R.string.pref_fare_entries_vehicles))) {
            avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_VEHICLE);
            price = cursor.getDouble(PriceListFragment.COL_PRICE_VEHICLE);
        }

        viewHolder.hourView.setText(hour);
        viewHolder.avgView.setText(String.valueOf(avg));
        viewHolder.priceView.setText(String.valueOf(price));
    }

    /**
     * Cache of the children views for a price list item.
     */
    public static class ViewHolder {
        public final TextView hourView;
        public final TextView avgView;
        public final TextView priceView;

        public ViewHolder(View view) {
            hourView = (TextView) view.findViewById(R.id.item_price_list_hour);
            avgView = (TextView) view.findViewById(R.id.item_price_list_avg);
            priceView = (TextView) view.findViewById(R.id.item_price_list_price);
        }
    }
}
