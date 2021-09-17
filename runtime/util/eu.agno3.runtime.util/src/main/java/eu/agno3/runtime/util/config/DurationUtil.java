/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 3, 2017 by mbechler
 */
package eu.agno3.runtime.util.config;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.Duration;


/**
 * Simple duration format parsing
 * 
 * @author mbechler
 *
 */
public final class DurationUtil {

    /**
     * 
     */
    private DurationUtil () {}

    private static final Map<String, Long> SUFFIXES = new HashMap<>();

    static {
        SUFFIXES.put("ms", 1L); //$NON-NLS-1$
        SUFFIXES.put("s", 1000L); //$NON-NLS-1$
        SUFFIXES.put("min", 60 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("h", 60 * 60 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("d", 24 * 60 * 60 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("w", 7 * 24 * 60 * 60 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("m", 31 * 24 * 60 * 60 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("y", 365 * 24 * 60 * 60 * 1000L); //$NON-NLS-1$
    }


    private static String getSuffix ( String s ) {
        int last = s.length() - 1;
        if ( Character.isDigit(s.charAt(last)) ) {
            return null;
        }

        for ( int i = last - 1; i >= 0; i-- ) {
            if ( Character.isDigit(s.charAt(i)) ) {
                return s.substring(i + 1);
            }
        }
        return null;
    }


    /**
     * 
     * @param s
     * @return whether this looks like a duration
     */
    public static boolean isDuration ( String s ) {
        String suffix = getSuffix(s);
        return suffix != null && SUFFIXES.containsKey(suffix.toLowerCase(Locale.ROOT));
    }


    /**
     * 
     * @param s
     * @return parsed duration
     */
    public static Duration parseDuration ( String s ) {
        return Duration.millis(getMillis(s));
    }


    /**
     * @param s
     * @return milliseconds in duration
     */
    private static long getMillis ( String s ) {
        String suffix = getSuffix(s);
        if ( suffix == null || !SUFFIXES.containsKey(suffix.toLowerCase(Locale.ROOT)) ) {
            throw new IllegalArgumentException("Invalid duration format " + s); //$NON-NLS-1$
        }

        String number = s.substring(0, s.length() - suffix.length());
        if ( !StringUtils.isNumeric(number) ) {
            throw new IllegalArgumentException("Not a number. Invalid duration " + s); //$NON-NLS-1$
        }

        long duration = Long.parseUnsignedLong(number);
        long mult = SUFFIXES.get(suffix.toLowerCase(Locale.ROOT));
        long millis = duration * mult;
        return millis;
    }


    /**
     * Parse duration in ISO 8601/millisecond or simple format
     * 
     * @param val
     * @return parsed value
     */
    public static Duration parseDurationCompat ( String val ) {
        if ( val.charAt(0) == 'P' ) {
            // ISO 8601 duration format
            return Duration.parse(val);
        }
        else if ( StringUtils.isNumeric(val) ) {
            // fully numeric, treat as millis
            return Duration.millis(Long.parseLong(val));
        }
        else if ( isDuration(val) ) {
            return parseDuration(val);
        }
        else {
            throw new IllegalArgumentException("Unsupported duration format: " + val); //$NON-NLS-1$
        }
    }
}
