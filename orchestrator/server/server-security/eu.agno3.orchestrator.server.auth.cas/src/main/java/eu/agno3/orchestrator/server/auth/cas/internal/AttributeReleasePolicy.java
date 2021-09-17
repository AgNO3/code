/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2016 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.services.RegisteredServiceAttributeFilter;
import org.jasig.cas.services.RegisteredServiceAttributeReleasePolicy;


/**
 * @author mbechler
 *
 */
final class AttributeReleasePolicy implements RegisteredServiceAttributeReleasePolicy {

    /**
     * 
     */
    private static final long serialVersionUID = -3082925871050698030L;

    private RegisteredServiceAttributeFilter filter;
    private final Set<String> allowedAttributes = new HashSet<>(Arrays.asList(
        "realmName", //$NON-NLS-1$
        "userName", //$NON-NLS-1$
        "userId", //$NON-NLS-1$
        "roles", //$NON-NLS-1$
        "permissions", //$NON-NLS-1$
        "authServerName" //$NON-NLS-1$
    ));


    /**
     * 
     */
    public AttributeReleasePolicy () {}


    @Override
    public void setAttributeFilter ( RegisteredServiceAttributeFilter filter ) {
        this.filter = filter;
    }


    @Override
    public boolean isAuthorizedToReleaseProxyGrantingTicket () {
        return false;
    }


    @Override
    public boolean isAuthorizedToReleaseCredentialPassword () {
        return false;
    }


    @Override
    public Map<String, Object> getAttributes ( Principal princ ) {
        Map<String, Object> attrs = new HashMap<>();
        for ( Entry<String, Object> attr : princ.getAttributes().entrySet() ) {
            if ( this.allowedAttributes.contains(attr.getKey()) ) {
                attrs.put(attr.getKey(), attr.getValue());
            }
        }
        if ( this.filter != null ) {
            return this.filter.filter(attrs);
        }
        return attrs;
    }
}