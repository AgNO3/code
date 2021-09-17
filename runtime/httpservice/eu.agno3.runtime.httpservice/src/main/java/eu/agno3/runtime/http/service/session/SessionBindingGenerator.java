/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 29, 2016 by mbechler
 */
package eu.agno3.runtime.http.service.session;


import javax.servlet.http.HttpServletRequest;


/**
 * @author mbechler
 *
 */
public interface SessionBindingGenerator {

    /**
     * Key for the session key
     */
    String SESSION_KEY = "agno3.sessionKey"; //$NON-NLS-1$


    /**
     * @param httpReq
     * @return hash to match in session
     */
    byte[] generateHash ( HttpServletRequest httpReq );

}
