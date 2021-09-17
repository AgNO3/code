/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2016 by mbechler
 */
package eu.agno3.runtime.jsf.i18n;


import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
@ApplicationScoped
@Named ( "_bm" )
public class BaseMessages extends FacesMessageBundle {

    private static final String MESSAGES_BASE = "eu.agno3.runtime.jsf.i18n.messages";


    public static String get ( String key ) {
        return get(MESSAGES_BASE, key);
    }


    public static String get ( String key, Locale l ) {
        return get(MESSAGES_BASE, key, l);
    }


    public static String format ( String key, Object... args ) {
        return format(MESSAGES_BASE, key, args);
    }

}
