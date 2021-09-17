/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2014 by mbechler
 */
package eu.agno3.runtime.i18n;


import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;


/**
 * @author mbechler
 * 
 */
public final class I18NUtil {

    private I18NUtil () {}


    /**
     * @param b
     * @param l
     * @param key
     * @param args
     * @return the message template formatted to the given locale
     */
    public static String format ( ResourceBundle b, Locale l, String key, Object... args ) {
        return format(b.getString(key), l, args);
    }


    /**
     * @param pattern
     * @param l
     * @param args
     * @return the message template interpolate in the given locale
     */
    public static String format ( String pattern, Locale l, Object... args ) {
        MessageFormat fmt = new MessageFormat(pattern, l);
        return fmt.format(args, new StringBuffer(), null).toString();
    }
}
