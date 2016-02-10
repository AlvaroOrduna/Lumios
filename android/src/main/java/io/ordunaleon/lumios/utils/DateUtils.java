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

package io.ordunaleon.lumios.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateUtils {

    /**
     * Values indicating what type of truncate is used, and consequently,
     * which fields we have to truncate.
     *
     * @see #getTruncateFields
     */
    public static final int TRUNCATE_UNIT_DAY = 0;
    public static final int TRUNCATE_UNIT_HOUR = 1;

    /**
     * Fields to truncate when we round a date to the day of month.
     * For instance: from 2016-10-21T21:03:49Z to 2016-10-21T00:00:00Z
     */
    private static final int[] TRUNCATE_FIELDS_DAY = {
            Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
    };

    /**
     * Fields to truncate when we round a date to the hour of day.
     * For instance: from 2016-10-21T21:03:49Z to 2016-10-21T21:00:00Z
     */
    private static final int[] TRUNCATE_FIELDS_HOUR = {
            Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND
    };

    private static final String DF_ESIOS = "dd/MM/yyyy";
    private static final String DF_ISO8601 = "yyyy-MM-dd'T'HH:mmZ";

    private static final TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");

    private static final Locale LOCALE_SPAIN = new Locale("es", "ES");

    /**
     * Set to zero each field given by fields array in th given calendar
     *
     * @param calendar     Calendar to modify.
     * @param truncateUnit Unit from which truncate (not including the given).
     * @return Calendar with truncated date.
     */
    private static Calendar truncate(Calendar calendar, int truncateUnit) {
        int[] fields = getTruncateFields(truncateUnit);
        for (int field : fields) {
            calendar.add(field, -1 * calendar.get(field));
        }

        return calendar;
    }

    /**
     * Get fields to truncate
     *
     * @param truncateUnit unit from which truncate (not including the given)
     * @return array of fields to truncate
     */
    private static int[] getTruncateFields(int truncateUnit) {
        switch (truncateUnit) {
            case TRUNCATE_UNIT_DAY:
                return TRUNCATE_FIELDS_DAY;
            case TRUNCATE_UNIT_HOUR:
                return TRUNCATE_FIELDS_HOUR;
        }

        throw new IllegalArgumentException(truncateUnit + " is not a valid truncate unit");
    }

    /**
     * Get now date string in ISO 8601 format truncated at the given unit.
     *
     * @param truncateUnit Unit from which truncate (not including the given).
     * @return Now date string in ISO 8601 format truncated at the given unit.
     */
    public static String getNowIso(int truncateUnit) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DF_ISO8601);
        sdf.setTimeZone(TZ_UTC);

        gc = (GregorianCalendar) truncate(gc, truncateUnit);

        return sdf.format(gc.getTime());
    }

    /**
     * Get tomorrow date string in ISO 8601 format truncated at the given unit.
     *
     * @param truncateUnit Unit from which truncate (not including the given).
     * @return Tomorrow date string in ISO 8601 format truncated at the given unit.
     */
    public static String getTomorrowIso(int truncateUnit) {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DF_ISO8601);
        sdf.setTimeZone(TZ_UTC);

        gc.add(Calendar.DAY_OF_MONTH, 1);
        gc = (GregorianCalendar) truncate(gc, truncateUnit);

        return sdf.format(gc.getTime());
    }

    /**
     * Given a date as a string in ISO 8061 format, extract one of its fields.
     *
     * @param dateStr Date as a string in ISO 8061 format.
     * @param field   Field to be extracted.
     * @return String containing the value of the required field.
     * @throws ParseException when the given string is not in the correct form.
     */
    public static String getFieldFromIsoDate(String dateStr, int field) throws ParseException {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DF_ISO8601, Locale.getDefault());
        Date date = sdf.parse(dateStr);
        gc.setTime(date);
        return String.valueOf(gc.get(field));
    }

    /**
     * Given a date as a string in Esios server format, return date as a string in ISO 8061 format
     * for UTC time zone.
     *
     * @param day  String containing the day, month and year of the date {@link DateUtils#DF_ESIOS}.
     * @param hour String containing the hour of the date.
     * @return Date as a string in ISO 8061 format for UTC time zone.
     * @throws ParseException when the given string is not in the correct form.
     */
    public static String getUtcIsoFromEsiosDate(String day, String hour) throws ParseException {
        GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DF_ESIOS, LOCALE_SPAIN);

        // Set day of the calendar.
        gc.setTime(sdf.parse(day));

        // Set hour of the calendar.
        gc.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hour.substring(0, 2)));

        // Change pattern and time zone of the sdf object.
        sdf.applyPattern(DF_ISO8601);
        sdf.setTimeZone(TZ_UTC);

        // Get date in ISO format for UTC time zone.
        return sdf.format(gc.getTime());
    }
}
