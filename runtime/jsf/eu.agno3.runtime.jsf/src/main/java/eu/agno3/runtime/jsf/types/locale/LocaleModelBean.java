/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.types.locale;


import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Named ( "localeModelBean" )
public class LocaleModelBean {

    private static Map<Locale, List<Locale>> LOCALE_CACHE = new HashMap<>();


    /**
     * @return
     */
    private static List<Locale> getLocaleCache ( Locale userLocale ) {
        List<Locale> cached = LOCALE_CACHE.get(userLocale);
        if ( cached != null ) {
            return cached;
        }

        List<Locale> locales = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(locales, new LocaleComparator(userLocale));
        cached = locales;
        LOCALE_CACHE.put(userLocale, cached);
        return locales;
    }


    /**
     * @return the list of known locales
     */
    public List<Locale> getModel () {
        return getLocaleCache(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }


    /**
     * @param l
     * @return the localized description of the locale
     */
    public String getLocaleLabel ( Locale l ) {
        return l.getDisplayName(FacesContext.getCurrentInstance().getViewRoot().getLocale());
    }
}
