/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.08.2014 by mbechler
 */
package eu.agno3.orchestrator.jsf.types.net;


import java.util.Locale;

import eu.agno3.runtime.jsf.i18n.FacesMessageBundle;


/**
 * @author mbechler
 * 
 */
public final class NetTypesMessages extends FacesMessageBundle {

    /**
     * 
     */
    public static final String TYPES_MESSAGES_BASE = "eu.agno3.orchestrator.jsf.types.net.messages"; //$NON-NLS-1$

    /**
     * 
     */
    public static final String IDN_INVALID = "idn.invalid"; //$NON-NLS-1$


    private NetTypesMessages () {}


    /**
     * 
     * @param key
     *            message id
     * @return the message localized according to the JSF ViewRoot locale
     */
    public static String get ( String key ) {
        return get(TYPES_MESSAGES_BASE, key);
    }


    /**
     * 
     * @param key
     *            message id
     * @param l
     *            desired locale
     * @return the message localized according to the given locale
     */
    public static String get ( String key, Locale l ) {
        return get(TYPES_MESSAGES_BASE, key, l);
    }

}
