/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 26, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import eu.agno3.runtime.crypto.CryptoException;


/**
 * @author mbechler
 *
 */
public interface TLSContextProvider {

    /**
     * Creates a new context for the configuration
     * 
     * @param cfg
     * @return context for the configuration
     * @throws CryptoException
     */
    TLSContext getContext ( InternalTLSConfiguration cfg ) throws CryptoException;


    /**
     * Updates the given context with changes in the configuration
     * 
     * @param cfg
     * @param ctx
     * @throws CryptoException
     */
    void update ( InternalTLSConfiguration cfg, TLSContext ctx ) throws CryptoException;

}
