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

package io.ordunaleon.lumios.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.ordunaleon.lumios.R;
import io.ordunaleon.lumios.adapter.PriceListAdapter;
import io.ordunaleon.lumios.utils.DateUtils;
import io.ordunaleon.lumios.utils.PrefUtils;

import static io.ordunaleon.lumios.data.LumiosContract.PriceEntry;

public class PriceListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRICE_LIST_LOADER = 0;

    public static final int COL_ID = 0;
    public static final int COL_DATE = 1;
    public static final int COL_PRICE = 2;
    public static final int COL_AVG = 3;
    public static final int COL_INCREASE = 4;

    private static final String SELECTED_KEY = "selected_position";

    private PriceListAdapter mPriceListAdapter;

    private ListView mListView;

    private int mPosition = ListView.INVALID_POSITION;

    public static PriceListFragment newInstance() {
        return new PriceListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The PriceListAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mPriceListAdapter = new PriceListAdapter(getActivity(), null, 0);

        // Inflate the layout for this fragment.
        View rootView = inflater.inflate(R.layout.fragment_price_list, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) rootView.findViewById(R.id.price_list_listview);
        mListView.setAdapter(mPriceListAdapter);

        // Restore mPosition, if exists
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(PRICE_LIST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When device rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to ListView.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }

        super.onSaveInstanceState(outState);
    }

    public void onFareChanged() {
        getLoaderManager().restartLoader(PRICE_LIST_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // Only show data for the current and subsequent time.
        String startDate = DateUtils.getNowIso(DateUtils.TRUNCATE_UNIT_HOUR);
        Uri uri = PriceEntry.buildUriWithStartDate(startDate);

        // Get the columns based on the selected fare.
        String[] projection = PriceEntry
                .getColumns(getActivity(), PrefUtils.getFareName(getActivity()));

        // Sort by date ascending.
        String sortOrder = PriceEntry.COLUMN_DATE + " ASC";

        return new CursorLoader(getActivity(),
                uri,
                projection,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPriceListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPriceListAdapter.swapCursor(null);
    }
}
