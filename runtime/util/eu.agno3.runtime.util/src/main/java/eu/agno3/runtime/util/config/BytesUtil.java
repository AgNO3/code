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


/**
 * @author mbechler
 *
 */
public final class BytesUtil {

    /**
     * 
     */
    private BytesUtil () {}

    private static final Map<String, Long> SUFFIXES = new HashMap<>();

    static {
        SUFFIXES.put("b", 1L); //$NON-NLS-1$
        SUFFIXES.put("kb", 1000L);//$NON-NLS-1$
        SUFFIXES.put("mb", 1000 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("gb", 1000 * 1000 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("tb", 1000 * 1000 * 1000 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("pb", 1000 * 1000 * 1000 * 1000 * 1000L); //$NON-NLS-1$
        SUFFIXES.put("eb", 1000 * 1000 * 1000 * 1000 * 1000 * 1000L); //$NON-NLS-1$
        // larger will overflow

        SUFFIXES.put("kib", 1024L); //$NON-NLS-1$
        SUFFIXES.put("mib", 1024 * 1024L); //$NON-NLS-1$
        SUFFIXES.put("gib", 1024 * 1024 * 1024L); //$NON-NLS-1$
        SUFFIXES.put("tib", 1024 * 1024 * 1024 * 1024L); //$NON-NLS-1$
        SUFFIXES.put("pib", 1024 * 1024 * 1024 * 1024 * 1024L); //$NON-NLS-1$
        SUFFIXES.put("eib", 1024 * 1024 * 1024 * 1024 * 1024 * 1024L); //$NON-NLS-1$
        // larger will overflow
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
     * @return whether this looks like a byte size
     */
    public static boolean isSize ( String s ) {
        String suffix = getSuffix(s);
        return suffix != null && SUFFIXES.containsKey(suffix.toLowerCase(Locale.ROOT));
    }


    /**
     * 
     * @param s
     * @return parsed duration
     */
    public static long parseSize ( String s ) {
        return getBytes(s);
    }


    /**
     * @param s
     * @return milliseconds in duration
     */
    private static long getBytes ( String s ) {
        String suffix = getSuffix(s);
        if ( suffix == null || !SUFFIXES.containsKey(suffix.toLowerCase(Locale.ROOT)) ) {
            throw new IllegalArgumentException("Invalid size format " + s); //$NON-NLS-1$
        }

        String number = s.substring(0, s.length() - suffix.length());
        if ( !StringUtils.isNumeric(number) ) {
            throw new IllegalArgumentException("Not a number. Invalid size " + s); //$NON-NLS-1$
        }

        long val = Long.parseUnsignedLong(number);
        long mult = SUFFIXES.get(suffix.toLowerCase(Locale.ROOT));
        return val * mult;
    }


    /**
     * Parse byte size in bytes or simple format
     * 
     * @param val
     * @return parsed size
     */
    public static Long parseByteSizeCompat ( String val ) {
        if ( StringUtils.isBlank(val) ) {
            return null;
        }
        else if ( StringUtils.isNumeric(val) ) {
            return Long.parseLong(val);
        }
        else if ( isSize(val) ) {
            return parseSize(val);
        }
        else {
            throw new IllegalArgumentException("Unsupported size format: " + val); //$NON-NLS-1$
        }
    }
}
