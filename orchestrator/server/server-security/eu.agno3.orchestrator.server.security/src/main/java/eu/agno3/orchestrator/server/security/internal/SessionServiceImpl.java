/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jws.WebService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.security.auth.login.CredentialExpiredException;

import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.joda.time.DateTime;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.security.impl.PreferenceStorage;
import eu.agno3.orchestrator.server.session.SessionException;
import eu.agno3.orchestrator.server.session.SessionInfo;
import eu.agno3.orchestrator.server.session.service.SessionService;
import eu.agno3.orchestrator.server.session.service.SessionServiceDescriptor;
import eu.agno3.runtime.security.DynamicModularRealmAuthorizer;
import eu.agno3.runtime.security.cas.client.CasToken;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.ws.common.SOAPWebService;
import eu.agno3.runtime.ws.server.Anonymous;
import eu.agno3.runtime.ws.server.WebServiceAddress;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    SessionService.class, SOAPWebService.class
} )
@WebService (
    endpointInterface = "eu.agno3.orchestrator.server.session.service.SessionService",
    targetNamespace = SessionServiceDescriptor.NAMESPACE,
    serviceName = "sessionService" )
@WebServiceAddress ( "/session" )
public class SessionServiceImpl implements SessionService {

    /**
     * 
     */
    private static final String AUTH_FAIL = "Failed to authenticate user"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(SessionServiceImpl.class);

    private DynamicModularRealmAuthorizer authorizer;

    private EntityManagerFactory orchestratorEMF;


    @Reference
    protected synchronized void setDynamicModularRealmAuthorizer ( DynamicModularRealmAuthorizer authz ) {
        this.authorizer = authz;
    }


    protected synchronized void unsetDynamicModularRealmAuthorizer ( DynamicModularRealmAuthorizer authz ) {
        if ( this.authorizer == authz ) {
            this.authorizer = null;
        }
    }


    @Reference ( service = EntityManagerFactory.class, target = "(persistenceUnit=orchestrator)" )
    protected synchronized void setEMF ( EntityManagerFactory emf ) {
        this.orchestratorEMF = emf;
    }


    protected synchronized void unsetEMF ( EntityManagerFactory emf ) {
        if ( this.orchestratorEMF == emf ) {
            this.orchestratorEMF = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.session.service.SessionService#keepAlive()
     */
    @Override
    @Anonymous
    public void keepAlive () throws SessionException {
        Subject s = SecurityUtils.getSubject();

        if ( s.isAuthenticated() ) {
            Session session = s.getSession(false);
            if ( session == null ) {
                throw new SessionException("Session timed out"); //$NON-NLS-1$
            }
        }
        else {
            throw new SessionException("No longer authenticated"); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.session.service.SessionService#login(java.lang.String)
     */
    @Override
    @Anonymous
    public SessionInfo login ( String ticketCredential ) throws SessionException, CredentialExpiredException {
        Subject s = SecurityUtils.getSubject();
        try {
            CasToken casProxyToken = new CasToken(ticketCredential);
            s.login(casProxyToken);
        }
        catch ( AuthenticationException e ) {
            log.warn(AUTH_FAIL, e);
            throw new CredentialExpiredException(AUTH_FAIL);
        }
        Session session = SecurityUtils.getSubject().getSession(true);
        SessionInfo sessInfo = createSessionInfo(s, session);
        if ( log.isDebugEnabled() ) {
            log.debug("Opened session for user " + sessInfo.getUserPrincipal()); //$NON-NLS-1$
            log.debug("Session id " + sessInfo.getSessionId()); //$NON-NLS-1$
            log.debug("Roles: " + sessInfo.getRoles()); //$NON-NLS-1$
            log.debug("Permissions: " + sessInfo.getPermissions()); //$NON-NLS-1$
        }
        return sessInfo;
    }


    /**
     * @param s
     * @param session
     * @return
     */
    protected SessionInfo createSessionInfo ( Subject s, Session session ) {
        Collection<UserPrincipal> userPrincs = s.getPrincipals().byType(UserPrincipal.class);

        if ( userPrincs.size() != 1 ) {
            throw new AuthenticationException("Did not get user principal from auth server"); //$NON-NLS-1$
        }

        UserPrincipal princ = userPrincs.iterator().next();
        SessionInfo sessInfo = new SessionInfo();
        sessInfo.setSessionId(session.getId().toString());
        sessInfo.setUserPrincipal(princ);
        sessInfo.setOpeningTime(new DateTime());
        sessInfo.setTimeout(session.getTimeout());

        AuthorizationInfo authzInfo = this.authorizer.getAuthorizationInfo(s.getPrincipals());
        if ( authzInfo.getRoles() != null ) {
            sessInfo.setRoles(new HashSet<>(authzInfo.getRoles()));
        }
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

        sessInfo.setPermissions(permissions);
        return sessInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.session.service.SessionService#logout()
     */
    @Override
    @Anonymous
    public void logout () throws SessionException {
        Subject s = SecurityUtils.getSubject();
        if ( log.isDebugEnabled() ) {
            log.debug("Logging out " + s.getPrincipal()); //$NON-NLS-1$
        }
        s.logout();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.session.service.SessionService#loadPreferences()
     */
    @Override
    @Anonymous
    public Map<String, String> loadPreferences () throws SessionException {
        Subject s = SecurityUtils.getSubject();
        UserPrincipal up = s.getPrincipals().oneByType(UserPrincipal.class);
        if ( !s.isAuthenticated() || up == null ) {
            return Collections.EMPTY_MAP;
        }

        EntityManager em = this.orchestratorEMF.createEntityManager();
        PreferenceStorage find = em.find(PreferenceStorage.class, up.getUserId());
        if ( find == null ) {
            return Collections.EMPTY_MAP;
        }
        return find.getPreferences();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.session.service.SessionService#savePreferences(java.util.Map)
     */
    @Override
    @Anonymous
    public Map<String, String> savePreferences ( Map<String, String> hashMap ) throws SessionException {
        Subject s = SecurityUtils.getSubject();
        UserPrincipal up = s.getPrincipals().oneByType(UserPrincipal.class);
        if ( !s.isAuthenticated() || up == null ) {
            throw new SessionException("Only authenticated users can save prefer"); //$NON-NLS-1$
        }

        EntityManager em = this.orchestratorEMF.createEntityManager();
        PreferenceStorage find = em.find(PreferenceStorage.class, up.getUserId());
        if ( find == null ) {
            find = new PreferenceStorage();
            find.setUserId(up.getUserId());
        }
        find.setPreferences(hashMap);
        em.persist(find);
        em.flush();
        em.refresh(find);
        return find.getPreferences();
    }

}
