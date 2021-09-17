/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.util.List;

import org.apache.shiro.authc.AuthenticationException;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.principal.UserPrincipal;
import eu.agno3.runtime.security.ratelimit.LoginRateLimiter;


/**
 * @author mbechler
 *
 */
public interface LoginRealmManager {

    /**
     * 
     * @return the rate limiter
     */
    LoginRateLimiter getRateLimiter ();


    /**
     * 
     * @return whether this is a multi realm configuration
     */
    boolean isMultiRealm ();


    /**
     * 
     * @param context
     * @return the applicable realms for the context
     */
    List<LoginRealm> getApplicableRealms ( LoginContext context );


    /**
     * @param loginContext
     * @return the applicable realm ids
     */
    List<String> getApplicableRealmIds ( LoginContext loginContext );


    /**
     * @param applicableRealms
     * @return the realms for the given ids
     */
    List<LoginRealm> mapRealmIds ( List<String> applicableRealms );


    /**
     * 
     * @param context
     * @return the default realm for the given context
     */
    LoginRealm getStaticDefaultRealm ( LoginContext context );


    /**
     * @param id
     * @return the realm
     */
    LoginRealm getRealm ( String id );


    /**
     * @return an audit context for login events
     */
    AuditContext<LoginEventBuilder> getAuditContext ();


    /**
     * @return whether to allow logins over insecure transports
     */
    boolean getAllowInsecureLogins ();


    /**
     * @param selectedLoginRealm
     * @param ctx
     * @param sess
     * @return authentication response
     * @throws AuthenticationException
     *             when an error occurs during authentication
     */
    AuthResponse authenticate ( LoginRealm selectedLoginRealm, LoginContext ctx, LoginSession sess );


    /**
     * @param primary
     * @param up
     * @param ctx
     * @param sess
     * @return authentication response
     * @throws AuthenticationException
     *             when an error occurs during authentication
     */
    AuthResponse changePassword ( LoginRealm primary, UserPrincipal up, LoginContext ctx, LoginSession sess );

}