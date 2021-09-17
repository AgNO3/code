/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.cas.client.AbstractCasRealm;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    Realm.class, AuthorizingRealm.class
} )
public class CASProxyAuthenticationRealm extends AbstractCasRealm {

    /**
     * 
     */
    private static final long serialVersionUID = 4349378206105453113L;

    private static final String API_CAS_CLIENT_TLS_SUBSYS = "api/casClient"; //$NON-NLS-1$

    private TLSContext tlsContext;
    private ServerAuthConfiguration authConfig;


    @Reference ( target = "(subsystem=api/casClient)" )
    protected synchronized void setTLSContext ( TLSContext tc ) {
        this.tlsContext = tc;
        super.setTlsContext(tc);
    }


    protected synchronized void unsetTLSContext ( TLSContext tc ) {
        if ( this.tlsContext == tc ) {
            this.tlsContext = null;
            super.setTlsContext(null);
        }
    }


    @Reference
    protected synchronized void setAuthConfig ( ServerAuthConfiguration cfg ) {
        this.authConfig = cfg;
        super.setAuthConfig(cfg);
    }


    protected synchronized void unsetAuthConfig ( ServerAuthConfiguration cfg ) {
        if ( this.authConfig == cfg ) {
            this.authConfig = null;
            super.setAuthConfig(null);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.AbstractCasRealm#postCasAuthenticate(java.lang.String,
     *      org.jasig.cas.client.authentication.AttributePrincipal, org.apache.shiro.subject.SimplePrincipalCollection)
     */
    @Override
    protected void postCasAuthenticate ( String ticket, AttributePrincipal principal, SimplePrincipalCollection principalCollection ) {
        String realmName = (String) principal.getAttributes().get("realmName"); //$NON-NLS-1$
        String userName = (String) principal.getAttributes().get("userName"); //$NON-NLS-1$
        String userIdAttr = (String) principal.getAttributes().get("userId"); //$NON-NLS-1$

        if ( StringUtils.isBlank(realmName) || StringUtils.isBlank(userName) || StringUtils.isBlank(userIdAttr) ) {
            throw new AuthenticationException("Did not recieve principal information from server"); //$NON-NLS-1$
        }

        String buildPrinc = String.format("%s@%s", userName, realmName); //$NON-NLS-1$
        if ( !buildPrinc.equals(principal.getName()) ) {
            throw new AuthenticationException(String.format(
                "Mismatch between CAS principal and user principal (CAS: %s, user: %s)", //$NON-NLS-1$
                principal.getName(),
                buildPrinc));
        }

        UUID userId = UUID.fromString(userIdAttr);
        principalCollection.add(new UserPrincipal(realmName, userId, userName), getName());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.AbstractCasRealm#getTlsSubsystem()
     */
    @Override
    protected String getTlsSubsystem () {
        return API_CAS_CLIENT_TLS_SUBSYS;
    }
}
