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

import io.ordunaleon.lumios.R;
import io.ordunaleon.lumios.ui.PriceListFragment;
import io.ordunaleon.lumios.utils.PrefUtils;

public class PriceListAdapter extends CursorAdapter {

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

        String date = cursor.getString(PriceListFragment.COL_PRICE_DATE);

        int fareIndex = PrefUtils.getFareIndex(context);
        double avg = 0;
        double price = 0;

        switch (fareIndex) {
            case 0:
                avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_GENERAL);
                price = cursor.getDouble(PriceListFragment.COL_PRICE_GENERAL);
                break;
            case 1:
                avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_NIGHT);
                price = cursor.getDouble(PriceListFragment.COL_PRICE_NIGHT);
                break;
            case 2:
                avg = cursor.getDouble(PriceListFragment.COL_PRICE_AVG_VEHICLE);
                price = cursor.getDouble(PriceListFragment.COL_PRICE_VEHICLE);
                break;
        }

        viewHolder.hourView.setText(date);
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
