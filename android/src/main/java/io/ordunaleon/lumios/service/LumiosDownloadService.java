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
import android.content.ContentValues;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.Vector;

import io.ordunaleon.lumios.utils.DateUtils;
import io.ordunaleon.lumios.utils.LogUtils;

import static io.ordunaleon.lumios.data.LumiosContract.PriceEntry;
import static io.ordunaleon.lumios.utils.LogUtils.LOGE;
import static io.ordunaleon.lumios.utils.LogUtils.LOGV;

public class LumiosDownloadService extends IntentService {

    private final String LOG_TAG = LogUtils.makeLogTag(this.getClass());

    protected final static String EXTRA_URL_KEY = "url";
    protected final static String EXTRA_URL_ARRAY_KEY = "url_array";

    /**
     * Creates a LumiosDownloadService. Invoked by your subclass's constructor.
     */
    public LumiosDownloadService() {
        super("LumiosDownloadService");
    }

    /**
     * Creates a LumiosDownloadService. Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public LumiosDownloadService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] urlStrArray = intent.getStringArrayExtra(EXTRA_URL_ARRAY_KEY);

        if (urlStrArray == null) {
            urlStrArray = new String[]{intent.getStringExtra(EXTRA_URL_KEY)};
        }

        HttpURLConnection urlConnection;
        BufferedReader reader;
        String rawData;

        // For each URL, download, parse and store data.
        for (String urlStr : urlStrArray) {
            urlConnection = null;
            reader = null;
            rawData = null;

            try {
                URL url = new URL(urlStr);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return;
                }

                rawData = buffer.toString();
            } catch (IOException e) {
                LOGE(LOG_TAG, "Error ", e);
                return;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        LOGE(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                // Parse and store data.
                processRawData(rawData);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void processRawData(String rawData) throws JSONException, ParseException {
        Gson gson = new Gson();

        // Get JSONArray from rawData.
        JSONObject rawDataJson = new JSONObject(rawData);
        JSONArray pricesJsonArray = rawDataJson.getJSONArray("PVPC");

        // Deserialize JSONArray into priceArray.
        Price[] priceArray = gson.fromJson(pricesJsonArray.toString(), Price[].class);

        Vector<ContentValues> cVVector = new Vector<>(priceArray.length);

        // Normalize data and calculate the average of these. Every time we do this is with
        // information of one day, so the average corresponds to the period of a day.
        double sumGeneral = 0;
        double sumNight = 0;
        double sumVehicle = 0;
        for (Price price : priceArray) {
            price.normalize();

            sumGeneral += price.getPriceGeneral();
            sumNight += price.getPriceNight();
            sumVehicle += price.getPriceVehicle();
        }

        double avgGeneral = sumGeneral / priceArray.length;
        double avgNight = sumNight / priceArray.length;
        double avgVehicle = sumVehicle / priceArray.length;
        for (Price price : priceArray) {
            price.setAvgGeneral(avgGeneral);
            price.setIncreaseGeneral((price.getPriceGeneral() / avgGeneral) * 100);

            price.setAvgNight(avgNight);
            price.setIncreaseNight((price.getPriceNight() / avgNight) * 100);

            price.setAvgVehicle(avgVehicle);
            price.setIncreaseVehicle((price.getPriceVehicle() / avgVehicle) * 100);

            cVVector.add(price.toContentValues());
        }

        LOGV(LOG_TAG, "Sync complete: " + priceArray.length + " new prices have been downloaded.");

        // Store values in database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            int result = getContentResolver().bulkInsert(PriceEntry.CONTENT_URI, cvArray);
            LOGV(LOG_TAG, "Store complete: " + result + " new prices have been stored in db.");
        }
    }

    public static class Price {

        // Attributes for deserialization.
        @SerializedName("Dia")
        private final String day;
        @SerializedName("Hora")
        private final String hour;
        @SerializedName("GEN")
        private final String general;
        @SerializedName("NOC")
        private final String night;
        @SerializedName("VHC")
        private final String vehicle;

        // Standardized attributes for storage.
        private String isoDateUTC;
        private double priceGeneral;
        private double avgGeneral;
        private double increaseGeneral;
        private double priceNight;
        private double avgNight;
        private double increaseNight;
        private double priceVehicle;
        private double avgVehicle;
        private double increaseVehicle;

        public Price(String day, String hour, String general, String night, String vehicle) {
            this.day = day;
            this.hour = hour;
            this.general = general;
            this.night = night;
            this.vehicle = vehicle;
        }

        @Override
        public String toString() {
            return "Price{" +
                    "isoDateUTC='" + isoDateUTC + '\'' +
                    ", priceGeneral=" + priceGeneral +
                    ", avgGeneral=" + avgGeneral +
                    ", priceNight=" + priceNight +
                    ", avgNight=" + avgNight +
                    ", priceVehicle=" + priceVehicle +
                    ", avgVehicle=" + avgVehicle +
                    '}';
        }

        public ContentValues toContentValues() {
            ContentValues cv = new ContentValues();

            cv.put(PriceEntry.COLUMN_DATE, isoDateUTC);
            cv.put(PriceEntry.COLUMN_PRICE_GENERAL, priceGeneral);
            cv.put(PriceEntry.COLUMN_AVG_GENERAL, avgGeneral);
            cv.put(PriceEntry.COLUMN_INCREASE_GENERAL, increaseGeneral);
            cv.put(PriceEntry.COLUMN_PRICE_NIGHT, priceNight);
            cv.put(PriceEntry.COLUMN_AVG_NIGHT, avgNight);
            cv.put(PriceEntry.COLUMN_INCREASE_NIGHT, increaseNight);
            cv.put(PriceEntry.COLUMN_PRICE_VEHICLE, priceVehicle);
            cv.put(PriceEntry.COLUMN_AVG_VEHICLE, avgVehicle);
            cv.put(PriceEntry.COLUMN_INCREASE_VEHICLE, increaseVehicle);

            return cv;
        }

        public void normalize() throws ParseException {
            // Store ISO date as new attribute.
            isoDateUTC = DateUtils.getUtcIsoFromEsiosDate(day, hour);

            // Normalize prices.
            priceGeneral = Double.parseDouble(general.replace(",", "."));
            priceNight = Double.parseDouble(night.replace(",", "."));
            priceVehicle = Double.parseDouble(vehicle.replace(",", "."));
        }

        public String getIsoDateUTC() {
            return isoDateUTC;
        }

        public double getPriceGeneral() {
            return priceGeneral;
        }

        public double getPriceNight() {
            return priceNight;
        }

        public double getPriceVehicle() {
            return priceVehicle;
        }

        public double getAvgGeneral() {
            return avgGeneral;
        }

        public void setAvgGeneral(double avgGeneral) {
            this.avgGeneral = avgGeneral;
        }

        public double getAvgNight() {
            return avgNight;
        }

        public void setAvgNight(double avgNight) {
            this.avgNight = avgNight;
        }

        public double getAvgVehicle() {
            return avgVehicle;
        }

        public void setAvgVehicle(double avgVehicle) {
            this.avgVehicle = avgVehicle;
        }

        public double getIncreaseGeneral() {
            return increaseGeneral;
        }

        public void setIncreaseGeneral(double increaseGeneral) {
            this.increaseGeneral = increaseGeneral;
        }

        public double getIncreaseNight() {
            return increaseNight;
        }

        public void setIncreaseNight(double increaseNight) {
            this.increaseNight = increaseNight;
        }

        public double getIncreaseVehicle() {
            return increaseVehicle;
        }

        public void setIncreaseVehicle(double increaseVehicle) {
            this.increaseVehicle = increaseVehicle;
        }
    }
}
