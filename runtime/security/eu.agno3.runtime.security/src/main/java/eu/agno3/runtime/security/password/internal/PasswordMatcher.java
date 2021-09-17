/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Collection;


/**
 * @author mbechler
 *
 */
public interface PasswordMatcher {

    /**
     * 
     * @param password
     * @return the matches
     */
    Collection<MatchEntry> match ( String password );
}
