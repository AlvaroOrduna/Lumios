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

package io.ordunaleon.lumios.service;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;

import io.ordunaleon.lumios.R;
import io.ordunaleon.lumios.utils.LogUtils;
import io.ordunaleon.lumios.utils.PrefUtils;

import static io.ordunaleon.lumios.utils.LogUtils.LOGD;
import static io.ordunaleon.lumios.utils.LogUtils.LOGI;

public class LumiosRegistrationIntentService extends IntentService {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    public LumiosRegistrationIntentService() {
        super("LumiosRegistrationIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // Getting token
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            LOGI(LOG_TAG, "GCM Registration Token: " + token);

            // For now, our server does not need any registration because we only use topics
            // subscriptions. If we ever need it, this would be the place to send the registration
            // request.

            // Subscribe to topic channels
            subscribeTopics(token);

            // Store a boolean indicating whether the app has been registered with GCM.
            PrefUtils.setAppRegistration(this, true);
        } catch (Exception e) {
            PrefUtils.setAppRegistration(this, false);

            LOGD(LOG_TAG, "Failed to complete token refresh", e);
        }
    }

    /**
     * Subscribe to any GCM topics of interest.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    private void subscribeTopics(String token) throws IOException {
        GcmPubSub pubSub = GcmPubSub.getInstance(this);
        pubSub.subscribe(token, "/topics/pvpc", null);
    }
}
