/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.02.2014 by mbechler
 */
package eu.agno3.runtime.jsf.i18n;


import java.util.Locale;
import java.util.ResourceBundle;

import javax.faces.context.FacesContext;

import eu.agno3.runtime.i18n.I18NUtil;


/**
 * @author mbechler
 * 
 */
public class FacesMessageBundle {

    protected FacesMessageBundle () {}


    /**
     * 
     * @param base
     * @param key
     * @param cl
     * @param args
     * @return the message localized and formatted according to the current JSF viewRoot locale
     */
    protected static String format ( String base, String key, Object... args ) {
        return I18NUtil.format(get(base, key), FacesContext.getCurrentInstance().getViewRoot().getLocale(), args);
    }


    /**
     * 
     * @param base
     * @param key
     * @param cl
     * @param args
     * @return the message localized and formatted according to the current JSF viewRoot locale
     */
    protected static String format ( String base, String key, ClassLoader cl, Object... args ) {
        return I18NUtil.format(get(base, key, cl), FacesContext.getCurrentInstance().getViewRoot().getLocale(), args);
    }


    /**
     * 
     * @param base
     * @param key
     * @param l
     * @param args
     * @return the message localized and formatted according to the given locale
     */
    protected static String format ( String base, String key, Locale l, Object... args ) {
        return I18NUtil.format(get(base, key, l), l, args);
    }


    /**
     * 
     * @param base
     * @param key
     * @param l
     * @param cl
     * @param args
     * @return the message localized and formatted according to the given locale
     */
    protected static String format ( String base, String key, Locale l, ClassLoader cl, Object... args ) {
        return I18NUtil.format(get(base, key, l, cl), l, args);
    }


    /**
     * 
     * @param base
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    protected static String get ( String base, String key ) {
        return FacesMessageBundle.get(base, key, FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * 
     * @param base
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    protected static String get ( String base, String key, ClassLoader cl ) {
        return FacesMessageBundle.get(base, key, FacesContext.getCurrentInstance().getViewRoot().getLocale(), cl);
    }


    /**
     * 
     * @param base
     * @param key
     *            message id
     * @param l
     *            desired locale
     * @return the message localized according to the given locale
     */
    protected static String get ( String base, String key, Locale l ) {
        return get(base, key, Thread.currentThread().getContextClassLoader());
    }


    /**
     * 
     * @param base
     * @param key
     * @param l
     * @param cl
     * @return the message localized according to the given locale
     */
    protected static String get ( String base, String key, Locale l, ClassLoader cl ) {
        return ResourceBundle.getBundle(base, l, cl).getString(key);
    }
}
