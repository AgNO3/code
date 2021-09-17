/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.03.2016 by mbechler
 */
package eu.agno3.orchestrator.server.auth.webapp;


import java.util.Locale;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public class AuthMessages extends FacesMessageBundle {

    private static final String AUTH_MESSAGES_BASE = "eu.agno3.orchestrator.server.auth.webapp.messages";


    public static String get ( String key ) {
        return get(AUTH_MESSAGES_BASE, key);
    }


    public static String get ( String key, Locale l ) {
        return get(AUTH_MESSAGES_BASE, key, l);
    }


    public static String format ( String key, Object... args ) {
        return format(AUTH_MESSAGES_BASE, key, args);
    }
}
