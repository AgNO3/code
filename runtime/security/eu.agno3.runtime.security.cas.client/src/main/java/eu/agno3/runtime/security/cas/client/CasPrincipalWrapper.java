/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.10.2014 by mbechler
 */
package eu.agno3.runtime.security.cas.client;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.ProxyRetriever;


/**
 * Wrapper around CAS Attribute Pricipal to make it serializable for session storage
 * 
 * @author mbechler
 *
 */
public class CasPrincipalWrapper implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5167034311710724842L;

    /**
     * 
     */
    private static final String PROXY_GRANTING_TICKET_FIELD = "proxyGrantingTicket"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(CasPrincipalWrapper.class);

    private Map<String, Serializable> attributes = new HashMap<>();
    private String principalName;
    private String proxyGrantingTicket;


    /**
     * @param casPrincipal
     */
    public CasPrincipalWrapper ( AttributePrincipalImpl casPrincipal ) {

        this.principalName = casPrincipal.getName();

        for ( Entry<String, Object> e : casPrincipal.getAttributes().entrySet() ) {
            if ( ! ( e.getValue() instanceof Serializable ) ) {
                log.warn(String.format("Principal attribute '%s' is not serializable, ignoring", e.getKey())); //$NON-NLS-1$
            }
            this.attributes.put(e.getKey(), (Serializable) e.getValue());
        }

        // HACK: cas client does not expose the proxyGrantingTicket in any way and attribute principal is not
        // serializable
        try {
            Field pgtField = casPrincipal.getClass().getDeclaredField(PROXY_GRANTING_TICKET_FIELD);
            pgtField.setAccessible(true);
            this.proxyGrantingTicket = (String) pgtField.get(casPrincipal);
        }
        catch (
            NoSuchFieldException |
            SecurityException |
            IllegalArgumentException |
            IllegalAccessException e ) {
            log.warn("Failed to extract proxyGrantingTicket from cas principal", e); //$NON-NLS-1$
        }

    }


    /**
     * @return the principalName
     */
    public String getPrincipalName () {
        return this.principalName;
    }


    /**
     * @return the attributes
     */
    public Map<String, Serializable> getAttributes () {
        return this.attributes;
    }


    /**
     * @return the proxyGrantingTicket
     */
    public String getProxyGrantingTicket () {
        return this.proxyGrantingTicket;
    }


    /**
     * @param proxyRetriever
     * @return an attribute principal
     */
    public AttributePrincipal getAttributePrincipal ( ProxyRetriever proxyRetriever ) {
        Map<String, Object> objectAttrs = new HashMap<>();
        objectAttrs.putAll(this.attributes);
        return new AttributePrincipalImpl(this.principalName, objectAttrs, this.proxyGrantingTicket, proxyRetriever);
    }
}
