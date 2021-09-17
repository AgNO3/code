/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login;


import java.io.IOException;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.security.event.LoginEventBuilder;
import eu.agno3.runtime.security.login.AuthResponse;
import eu.agno3.runtime.security.login.LoginContext;
import eu.agno3.runtime.security.login.LoginRealm;
import eu.agno3.runtime.security.login.LoginSession;


/**
 * @author mbechler
 *
 */
public interface CustomWebAuthRealm extends LoginRealm {

    /**
     * 
     * @return whether this realm supports password fallback
     */
    public boolean supportsPasswordFallback ();


    /**
     * @param returnParam
     * @param l
     * @return a message to display to the user
     */
    public String handleNonSuccessReturn ( String returnParam, Locale l );


    /**
     * @param config
     * @param loginContext
     * @param loginSession
     * @return if non null, redirect to the given path under the realms authentication path
     * @throws Exception
     */
    public String doAuthentication ( WebLoginConfig config, LoginContext loginContext, LoginSession loginSession ) throws Exception;


    /**
     * @param req
     * @param resp
     * @param audit
     * @return authentication response
     * @throws CustomWebAuthAuthenticationException
     * @throws IOException
     */
    public AuthResponse doExternalAuthentication ( HttpServletRequest req, HttpServletResponse resp, AuditContext<LoginEventBuilder> audit )
            throws CustomWebAuthAuthenticationException, IOException;
}
