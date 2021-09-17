/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2015 by mbechler
 */
package eu.agno3.runtime.webdav.server;


import javax.jcr.Credentials;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.collections4.MultiValuedMap;


/**
 * @author mbechler
 *
 */
public interface SessionFactory {

    /**
     * @param creds
     * @param workspaceName
     * @return a session
     */
    Session createSession ( Credentials creds, String workspaceName );


    /**
     * 
     * @return implementation options
     */
    MultiValuedMap<String, Value> getOptions ();

}
