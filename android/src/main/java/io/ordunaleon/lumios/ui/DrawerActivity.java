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

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import io.ordunaleon.lumios.R;
import io.ordunaleon.lumios.service.LumiosRegistrationIntentService;
import io.ordunaleon.lumios.utils.LogUtils;
import io.ordunaleon.lumios.utils.PrefUtils;

import static io.ordunaleon.lumios.utils.LogUtils.LOGI;


public class DrawerActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String KEY_STATE_TITLE = "state_title";

    // Id of the default item in the menu of the Navigation Drawer. Used to define how we
    // started the activity.
    private static final @IdRes int DEFAULT_DRAWER_ITEM_ID = R.id.drawer_item_main;

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private TextView mDrawerHeaderHead;
    private TextView mDrawerHeaderSubhead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Find and setup Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Find and setup DrawerLayout and it's ActionBarDrawerToggle.
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        // Find and setup NavigationView.
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Find NavigationView header.
        View headerView = navigationView.getHeaderView(0);

        // Make clickable the text in the headerView.
        LinearLayout headerTitle = (LinearLayout) headerView.findViewById(R.id.drawer_header_title);
        headerTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Todo: Allow the user to change the fare rather than display a Snackbar.
                View view = v.getRootView().findViewById(R.id.frame_layout);
                Snackbar.make(view, "WIP | Select your fare", Snackbar.LENGTH_SHORT).show();
            }
        });

        // Find and setup the headerView text.
        mDrawerHeaderHead = (TextView) headerTitle.findViewById(R.id.drawer_header_head);
        mDrawerHeaderSubhead = (TextView) headerTitle.findViewById(R.id.drawer_header_subhead);
        updateDrawerHeader();

        // First run of the app starts with the Navigation Drawer open.
        if (!PrefUtils.isWelcomeDone(this)) {
            PrefUtils.setWelcomeDone(this, true);
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

        if (savedInstanceState == null) {
            onNavigationItemSelected(navigationView.getMenu().findItem(DEFAULT_DRAWER_ITEM_ID));
        } else {
            setTitle(savedInstanceState.getCharSequence(KEY_STATE_TITLE));
        }

        // Check for Google Play Services and start LumiosRegistrationIntentService to register this
        // application with GCM if needed.
        if (checkPlayServices() && !PrefUtils.isAppRegistered(this)) {
            Intent intent = new Intent(this, LumiosRegistrationIntentService.class);
            startService(intent);
        }

        PrefUtils.registerOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the DrawerToggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PrefUtils.unregisterOnSharedPreferenceChangeListener(this, this);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(KEY_STATE_TITLE, getTitle());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Pass any configuration change to the DrawerToggle.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        Fragment fragment;
        switch (menuItem.getItemId()) {
            case R.id.drawer_item_main:
                fragment = PriceListFragment.newInstance();
                break;
            case R.id.drawer_item_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return false;
            default:
                fragment = DummyFragment.newInstance(menuItem.getTitle());
                break;
        }

        // Insert the fragment by replacing any existing fragment.
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frame_layout, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer.
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getResources().getString(R.string.pref_fare_key))) {
            updateDrawerHeader();

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
            if (fragment instanceof PriceListFragment) {
                ((PriceListFragment) fragment).onFareChanged();
            }
        }
    }

    private void updateDrawerHeader() {
        mDrawerHeaderSubhead.setText(PrefUtils.getFareName(this));
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                LOGI(LOG_TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
}
