/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.05.2015 by mbechler
 */
package eu.agno3.runtime.i18n;


import java.io.IOException;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;


/**
 * @author mbechler
 *
 */
public class StaticMapResourceBundleControl extends Control {

    private Map<Locale, String> msgs;


    /**
     * @param msgs
     * 
     */
    public StaticMapResourceBundleControl ( Map<Locale, String> msgs ) {
        this.msgs = msgs;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.ResourceBundle.Control#getTimeToLive(java.lang.String, java.util.Locale)
     */
    @Override
    public long getTimeToLive ( String baseName, Locale locale ) {
        return TTL_DONT_CACHE;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.ResourceBundle.Control#needsReload(java.lang.String, java.util.Locale, java.lang.String,
     *      java.lang.ClassLoader, java.util.ResourceBundle, long)
     */
    @Override
    public boolean needsReload ( String baseName, Locale locale, String format, ClassLoader loader, ResourceBundle bundle, long loadTime ) {
        return true;
    }


    /**
     * @return the msgs
     */
    Map<Locale, String> getMsgs () {
        return this.msgs;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String,
     *      java.lang.ClassLoader, boolean)
     */
    @Override
    public ResourceBundle newBundle ( String baseName, Locale locale, String format, ClassLoader loader, boolean reload )
            throws IllegalAccessException, InstantiationException, IOException {

        if ( !this.msgs.containsKey(locale) ) {
            return null;
        }

        final String msg = this.msgs.get(locale);
        return new ListResourceBundle() {

            @Override
            protected Object[][] getContents () {
                return new Object[][] {
                    {
                        "msg", msg //$NON-NLS-1$
                    }
                };
            }
        };
    }
}
