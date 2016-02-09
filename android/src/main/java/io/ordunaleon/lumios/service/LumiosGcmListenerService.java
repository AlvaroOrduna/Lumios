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

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GcmListenerService;

import io.ordunaleon.lumios.utils.LogUtils;

import static io.ordunaleon.lumios.utils.LogUtils.LOGD;

public class LumiosGcmListenerService extends GcmListenerService {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle data) {
        LOGD(LOG_TAG, "onMessageReceived from: " + from);

        if (from.equals("/topics/pvpc")) {
            Intent intent = new Intent(this, LumiosDownloadService.class);
            intent.putExtras(data);
            startService(intent);
        }
    }
}
