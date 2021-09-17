/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.auth.cas.internal;


import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.jasig.cas.authentication.AuthenticationHandler;
import org.jasig.cas.authentication.BasicCredentialMetaData;
import org.jasig.cas.authentication.Credential;
import org.jasig.cas.authentication.DefaultHandlerResult;
import org.jasig.cas.authentication.HandlerResult;
import org.jasig.cas.authentication.PreventedException;

import eu.agno3.orchestrator.server.auth.cas.ShiroCredential;
import eu.agno3.orchestrator.server.auth.cas.SimplePrincipal;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class ShiroAuthenticationHandler implements AuthenticationHandler {

    /**
     * 
     */
    private static final String SHIRO = "Shiro"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(ShiroAuthenticationHandler.class);

    private final DynamicModularRealmAuthorizer authorizer;
    private final boolean sendPermissions;


    /**
     * 
     */
    public ShiroAuthenticationHandler () {
        this.authorizer = null;
        this.sendPermissions = false;
    }


    /**
     * @param authorizer
     * @param sendPermissions
     */
    public ShiroAuthenticationHandler ( DynamicModularRealmAuthorizer authorizer, boolean sendPermissions ) {
        this.authorizer = authorizer;
        this.sendPermissions = sendPermissions;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#authenticate(org.jasig.cas.authentication.Credential)
     */
    @Override
    public HandlerResult authenticate ( Credential credential ) throws GeneralSecurityException, PreventedException {
        Subject s = SecurityUtils.getSubject();

        if ( !s.isAuthenticated() ) {
            log.debug("Not authenticated"); //$NON-NLS-1$
            throw new FailedLoginException();
        }

        UserPrincipal princ = getUserPrincipal(s);
        if ( log.isDebugEnabled() ) {
            log.debug("Authenticated as " + princ); //$NON-NLS-1$
        }

        BasicCredentialMetaData metaData = new BasicCredentialMetaData(credential);
        Map<String, Object> princAttrs = new HashMap<>();

        princAttrs.put("userId", princ.getUserId().toString()); //$NON-NLS-1$
        princAttrs.put("userName", princ.getUserName()); //$NON-NLS-1$
        princAttrs.put("realmName", princ.getRealmName()); //$NON-NLS-1$

        HttpServletRequest req = WebUtils.getHttpRequest(s);
        if ( req != null ) {
            princAttrs.put("authServerName", req.getServerName()); //$NON-NLS-1$
        }

        if ( this.authorizer != null ) {
            addRolesAndPermissions(s, princAttrs);
        }

        SimplePrincipal p = new SimplePrincipal(s.getPrincipal().toString(), princAttrs);
        return new DefaultHandlerResult(this, metaData, p);
    }


    /**
     * @param s
     * @param princAttrs
     */
    private void addRolesAndPermissions ( Subject s, Map<String, Object> princAttrs ) {
        SimpleAuthorizationInfo authzInfo = this.authorizer.getAuthorizationInfo(s.getPrincipals());
        if ( authzInfo.getRoles() != null ) {
            princAttrs.put("roles", StringUtils.join(authzInfo.getRoles(), ',')); //$NON-NLS-1$
        }

        if ( this.sendPermissions ) {
            Set<String> permissions = new HashSet<>();
            if ( authzInfo.getStringPermissions() != null ) {
                permissions.addAll(authzInfo.getStringPermissions());
            }

            if ( authzInfo.getObjectPermissions() != null ) {
                for ( Permission perm : authzInfo.getObjectPermissions() ) {
                    String str = perm.toString();
                    if ( str.charAt(0) == '[' && str.charAt(str.length() - 1) == ']' ) {
                        str = str.substring(1, str.length() - 1);
                    }
                    permissions.add(str);
                }
            }
            princAttrs.put("permissions", StringUtils.join(permissions, ',')); //$NON-NLS-1$
        }
    }


    /**
     * @param s
     * @return
     * @throws GeneralSecurityException
     */
    protected UserPrincipal getUserPrincipal ( Subject s ) throws GeneralSecurityException {
        PrincipalCollection col = s.getPrincipals();

        if ( col == null ) {
            throw new GeneralSecurityException("Failed to get principal collection from subject"); //$NON-NLS-1$

        }

        Collection<UserPrincipal> princs = col.byType(UserPrincipal.class);

        if ( princs.size() != 1 ) {
            throw new GeneralSecurityException("Failed to get UserPrincipal"); //$NON-NLS-1$
        }
        return princs.iterator().next();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#supports(org.jasig.cas.authentication.Credential)
     */
    @Override
    public boolean supports ( Credential credential ) {
        return credential instanceof ShiroCredential;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.jasig.cas.authentication.AuthenticationHandler#getName()
     */
    @Override
    public String getName () {
        return SHIRO;
    }

}
