/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.util.i18n;


import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public final class I18NUtil {

    private static final Logger log = Logger.getLogger(I18NUtil.class);


    /**
     * 
     */
    private I18NUtil () {}


    /**
     * 
     * @param b
     * @param key
     * @param args
     * @return the pattern key from bundle formatted in the current JSF ViewRoot locale
     */
    public static String format ( ResourceBundle b, String key, Object... args ) {
        return format(b.getString(key), args);
    }


    /**
     * @param pattern
     * @param args
     * @return the pattern formatted in the current JSF ViewRoot locale
     */
    public static String format ( String pattern, Object... args ) {
        return eu.agno3.runtime.i18n.I18NUtil.format(pattern, FacesContext.getCurrentInstance().getViewRoot().getLocale(), args);
    }


    public static <TEnum extends Enum<TEnum>> String translateEnumValue ( ResourceBundle b, Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        try {
            return b.getString(key.toString());
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing resource", e); //$NON-NLS-1$
            return enumVal.name();
        }
    }


    public static <TEnum extends Enum<TEnum>> String translateEnumDescription ( ResourceBundle b, Class<TEnum> en, Object val ) {
        if ( val == null || !en.isAssignableFrom(val.getClass()) ) {
            return null;
        }
        @SuppressWarnings ( "unchecked" )
        TEnum enumVal = (TEnum) val;
        StringBuilder key = new StringBuilder();
        key.append(en.getSimpleName());
        key.append('.');
        key.append(enumVal.name());
        key.append(".description"); //$NON-NLS-1$
        try {
            return b.getString(key.toString());
        }
        catch ( MissingResourceException e ) {
            log.debug("Missing description resource", e); //$NON-NLS-1$
            return null;
        }
    }

}
