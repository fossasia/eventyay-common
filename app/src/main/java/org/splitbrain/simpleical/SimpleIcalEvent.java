package org.splitbrain.simpleical;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hold a single iCal VEvent
 *
 * @author Andreas Gohr <andi@splitbrain.org>
 */
public class SimpleIcalEvent {
    private final HashMap<String, IcalEventFacet> data;

    /**
     * Constructor
     */
    public SimpleIcalEvent() {
        data = new HashMap<String, IcalEventFacet>();
    }

    /**
     * Convenience method to get the DTSTART key as a Date
     *
     * @return null if the field isn't set or can't be parsed
     */
    public Date getStartDate() {
        String date = get("DTSTART");
        if (date == null) return null;

        try {
            return parseDate(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Convenience method to get the DTEND key as a Date
     *
     * @return null if the field isn't set or can't be parsed
     */
    public Date getEndDate() {
        String date = get("DTEND");

        // no end set, try to use a duration
        if (date == null) {
            String duration = get("DURATION");
            if (duration == null) return null;

            Date start = getStartDate();
            if (start == null) return null;

            return new Date(start.getTime() + parseDuration(duration));
        }

        try {
            return parseDate(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Simplistic ical date parser
     *
     * @param date A string in ical format
     * @return the parsed date
     * @throws java.text.ParseException if the parsing fails
     */
    public static Date parseDate(String date) throws ParseException {
        date = date.replaceAll("Z$", "-0000");
        date = date.replaceAll("([0-9][0-9]):([0-9][0-9])$", "$1$2");

        Date dt;
        try {
            // with timezone first
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
            dt = df.parse(date);
        } catch (ParseException e) {
            // no timezone
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            dt = df.parse(date);
        }
        return dt;
    }

    /**
     * Simplistic ical duration parser
     * <p/>
     * Will return 0 if parsing fails
     *
     * @param duration A string containing a duration in ical format
     * @return the parsed duration in milli seconds
     */
    public static long parseDuration(String duration) {
        long length = 0;
        Pattern pattern = Pattern.compile("(\\+|-)?(P([0-9]+W)?([0-9]+D)?)?(T([0-9]+H)?([0-9]+M)?([0-9]+S)?)?");
        Matcher matcher = pattern.matcher(duration);
        if (matcher.find()) {
            if (matcher.group(3) != null) {
                length += Integer.parseInt(matcher.group(3).substring(0, matcher.group(3).length() - 1)) * 7 * 24 * 60 * 60; // weeks
            }
            if (matcher.group(4) != null) {
                length += Integer.parseInt(matcher.group(4).substring(0, matcher.group(4).length() - 1)) * 24 * 60 * 60; // days
            }
            if (matcher.group(6) != null) {
                length += Integer.parseInt(matcher.group(6).substring(0, matcher.group(6).length() - 1)) * 60 * 60; // hours
            }
            if (matcher.group(7) != null) {
                length += Integer.parseInt(matcher.group(7).substring(0, matcher.group(7).length() - 1)) * 60; // minutes
            }
            if (matcher.group(8) != null) {
                length += Integer.parseInt(matcher.group(8).substring(0, matcher.group(8).length() - 1)); // seconds
            }

            // negative duration
            if (matcher.group(1) != null && matcher.group(1).equals("-")) length *= -1;
        }
        return length * 1000; // make it milli seconds
    }

    /**
     * Sets the value of the given key
     *
     * @param key the key
     * @param val the value
     */
    public void set(String key, String val) {
        key = key.toUpperCase();
        IcalEventFacet facet;
        if (data.containsKey(key)) {
            facet = data.get(key);
        } else {
            facet = new IcalEventFacet();
            data.put(key, facet);
        }

        facet.set(val);
    }

    /**
     * Sets a parameter value of a given key
     *
     * @param key the key
     * @param param the attribute
     * @param val the value
     */
    public void set(String key, String param, String val) {
        key = key.toUpperCase();
        param = param.toUpperCase();
        IcalEventFacet facet;
        if (data.containsKey(key)) {
            facet = data.get(key);
        } else {
            facet = new IcalEventFacet();
            data.put(key, facet);
        }

        facet.set(param, val);
    }

    /**
     * Returns the value of a given key
     *
     * @param key the key
     * @return null if the key doesn't exist
     */
    public String get(String key) {
        key = key.toUpperCase();
        if (data.containsKey(key)) {
            return data.get(key).get();
        } else {
            return null;
        }
    }

    /**
     * Returns a parameter value of a given key
     *
     * @param key the key
     * @param param the attribute
     * @return null if the key or parameter doesn't exist
     */
    public String get(String key, String param) {
        key = key.toUpperCase();
        param = param.toUpperCase();
        if (data.containsKey(key)) {
            return data.get(key).get(param);
        } else {
            return null;
        }
    }

    private class IcalEventFacet {
        private String value;
        private final HashMap<String, String> extra;

        public IcalEventFacet() {
            extra = new HashMap<String, String>();
        }

        public void set(String val) {
            value = val;
        }

        public String get() {
            return value;
        }

        public void set(String key, String val) {
            key = key.toUpperCase();
            extra.put(key, val);
        }

        public String get(String key) {
            key = key.toUpperCase();
            if (extra.containsKey(key)) {
                return extra.get(key);
            } else {
                return null;
            }
        }
    }
}
