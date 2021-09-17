/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.auth;


import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.jasig.cas.client.authentication.AttributePrincipalImpl;
import org.jasig.cas.client.proxy.ProxyGrantingTicketStorageImpl;
import org.jasig.cas.client.ssl.HttpURLConnectionFactory;

import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.security.cas.client.AbstractCasRealm;
import eu.agno3.runtime.security.cas.client.CasAuthConfiguration;
import eu.agno3.runtime.security.cas.client.CasPrincipalWrapper;


/**
 * @author mbechler
 *
 */
public class WebServiceAuthRealm extends AbstractCasRealm implements HttpURLConnectionFactory, SessionListener {

    /**
     * 
     */
    private static final long serialVersionUID = 3160116754164253976L;

    /**
     * 
     */
    private static final String BACKEND_AUTHFAIL = "Failed to authenticate with backend service"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(WebServiceAuthRealm.class);

    private transient GuiWsClientFactory sessContext;


    public WebServiceAuthRealm ( TLSContext context, CasAuthConfiguration authConfig, GuiWsClientFactory sessContext ) {
        // TODO: check encrypted storage PGT
        super(context, authConfig, new ProxyGrantingTicketStorageImpl());
        this.sessContext = sessContext;
        setName("WSClient"); //$NON-NLS-1$
    }


    /**
     * @param princs
     * @param authInfo
     */
    @Override
    protected void contributeAuthorizationInfo ( PrincipalCollection princs, SimpleAuthorizationInfo authInfo ) {
        SessionInfo sessionInfo = princs.oneByType(SessionInfo.class);
        if ( sessionInfo == null ) {
            throw new AuthorizationException("No session info present"); //$NON-NLS-1$
        }
        addServerRolesAndPermissions(sessionInfo, authInfo);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.cas.client.AbstractCasRealm#doGetAuthorizationInfo(org.apache.shiro.subject.PrincipalCollection)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo ( PrincipalCollection princs ) {
        SessionInfo sessionInfo = princs.oneByType(SessionInfo.class);
        if ( sessionInfo == null ) {
            throw new AuthorizationException("No session info present"); //$NON-NLS-1$
        }
        AuthorizationInfo authInfo = super.doGetAuthorizationInfo(princs);
        addServerRolesAndPermissions(sessionInfo, (SimpleAuthorizationInfo) authInfo);
        return authInfo;
    }


    /**
     * @param sessionInfo
     * @param authInfo
     */
    protected void addServerRolesAndPermissions ( SessionInfo sessionInfo, SimpleAuthorizationInfo authInfo ) {
        Set<String> roles = sessionInfo.getRoles();
        if ( roles != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Server returned roles are " + roles); //$NON-NLS-1$
            }
            authInfo.addRoles(roles);
        }

        Set<String> permissions = sessionInfo.getPermissions();
        if ( permissions != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Server returned permissions are " + permissions); //$NON-NLS-1$
            }
            authInfo.addStringPermissions(permissions);
        }
    }


    /**
     * @param ticket
     * @param principal
     * @param principalCollection
     */
    @Override
    protected void postCasAuthenticate ( String ticket, AttributePrincipal principal, SimplePrincipalCollection principalCollection ) {
        SessionInfo info;
        try {
            info = this.sessContext.login(principal);
        }
        catch ( Exception e ) {
            log.debug(BACKEND_AUTHFAIL, e);
            throw new AuthenticationException(BACKEND_AUTHFAIL, e);
        }
        principalCollection.add(new CasPrincipalWrapper((AttributePrincipalImpl) principal), getName());
        principalCollection.add(info, getName());
    }

}
