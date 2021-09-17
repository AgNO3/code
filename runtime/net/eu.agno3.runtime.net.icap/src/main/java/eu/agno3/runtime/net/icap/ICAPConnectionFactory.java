/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2017 by mbechler
 */
package eu.agno3.runtime.net.icap;


import eu.agno3.runtime.crypto.tls.TLSContext;


/**
 * @author mbechler
 *
 */
public interface ICAPConnectionFactory {

    /**
     * @param cfg
     * @param tc
     * @return connected icap connection
     * @throws ICAPException
     */
    ICAPConnection createICAPConnection ( ICAPConfiguration cfg, TLSContext tc ) throws ICAPException;

}
