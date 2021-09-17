/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.01.2015 by mbechler
 */
package eu.agno3.runtime.security.web.login.token;


import java.io.Serializable;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import eu.agno3.runtime.security.token.RealmTokenToken;


/**
 * @author mbechler
 *
 */
public interface TokenGenerator {

    /**
     * @param tokenData
     * @param expires
     * @return a token string
     * @throws TokenCreationException
     */
    String createToken ( Serializable tokenData, DateTime expires ) throws TokenCreationException;


    /**
     * 
     * @param tokenData
     * @param lifetime
     * @return a token string
     * @throws TokenCreationException
     */
    String createToken ( Serializable tokenData, Duration lifetime ) throws TokenCreationException;


    /**
     * @param tok
     * @return principals for this token
     * @throws AuthenticationException
     */
    PrincipalCollection validate ( RealmTokenToken tok ) throws AuthenticationException;

}
