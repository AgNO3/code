/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 21, 2017 by mbechler
 */
package eu.agno3.runtime.elasticsearch;


/**
 * @author mbechler
 *
 */
public interface ClientProvider {

    /**
     * 
     * @return client instance
     * @throws ClientException
     */
    Client client () throws ClientException;


    /**
     * @return whether clients returned by this provider can be used to perform administrative operations
     */
    boolean allowsAdminOperations ();
}
