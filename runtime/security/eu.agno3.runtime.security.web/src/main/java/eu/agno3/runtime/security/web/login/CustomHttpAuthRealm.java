/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;


/**
 * @author mbechler
 *
 */
public interface CustomHttpAuthRealm extends LoginRealm {

    /**
     * 
     * @return whether this realm supports password fallback
     */
    public boolean supportsPasswordFallback ();


    /**
     * @param req
     * @return WWW-authenticate challenges
     */
    public List<String> getChallenges ( HttpServletRequest req );


    /**
     * @param req
     * @param resp
     * @param loginContext
     * @param audit
     * @throws CustomWebAuthAuthenticationException
     * @return whether authentication has been handled
     * @throws IOException
     */
    public AuthResponse doHTTPAuthentication ( HttpServletRequest req, HttpServletResponse resp, LoginContext loginContext,
            AuditContext<LoginEventBuilder> audit ) throws CustomWebAuthAuthenticationException, IOException;

}
