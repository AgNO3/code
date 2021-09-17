/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.03.2014 by mbechler
 */
package eu.agno3.runtime.util.format;


/**
 * @author mbechler
 * 
 */
public final class ByteSizeFormatter {

    private static final String[] SI_UNITS = {
        "B", //$NON-NLS-1$
        "kB", //$NON-NLS-1$
        "MB", //$NON-NLS-1$
        "GB", //$NON-NLS-1$
        "TB", //$NON-NLS-1$
        "PB", //$NON-NLS-1$
        "EB", //$NON-NLS-1$
        "ZB", //$NON-NLS-1$
        "YB", //$NON-NLS-1$
    };

    private static final String[] UNITS = {
        "B", //$NON-NLS-1$
        "KiB", //$NON-NLS-1$
        "MiB", //$NON-NLS-1$
        "GiB", //$NON-NLS-1$
        "TiB", //$NON-NLS-1$
        "PiB", //$NON-NLS-1$
        "EiB", //$NON-NLS-1$
        "ZiB", //$NON-NLS-1$
        "YiB" //$NON-NLS-1$
    };


    private ByteSizeFormatter () {}


    /**
     * 
     * @param obj
     *            number of bytes
     * @return formatted result including unit (1024 based)
     */
    public static String formatByteSize ( Object obj ) {
        return format(obj, 1024, UNITS);
    }


    /**
     * 
     * @param obj
     *            number of bytes
     * @return formatted result including unit (1000 based)
     */
    public static String formatByteSizeSI ( Object obj ) {
        return format(obj, 1000, SI_UNITS);
    }


    protected static String format ( Object obj, int base, String[] units ) {
        long size = 0;

        if ( obj == null ) {
            return null;
        }

        if ( obj.getClass().isPrimitive() ) {
            size = (long) obj;
        }
        else if ( obj instanceof Long ) {
            size = (long) obj;
        }
        else if ( obj instanceof Integer ) {
            size = ( (Integer) obj ).longValue();
        }
        else if ( obj instanceof Short ) {
            size = ( (Short) obj ).longValue();
        }

        int exp = 0;
        double rounded = 0.0;
        if ( size > 0 ) {
            exp = (int) ( Math.log(size) / Math.log(base) );
            rounded = size / Math.pow(base, exp);
        }

        if ( exp >= units.length || exp < 0 ) {
            throw new IllegalArgumentException(String.format("Illegal size %d (%.1fE%d)", size, rounded, exp)); //$NON-NLS-1$
        }
        String unit = units[ exp ];
        return String.format("%.1f %s", rounded, unit); //$NON-NLS-1$
    }

}
