/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.truststore;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public TruststoresConfig createTruststoresConfig () {
        return new TruststoresConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public TruststoreConfig createTruststoreConfig () {
        return new TruststoreConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public RevocationConfig createRevocationConfig () {
        return new RevocationConfigImpl();
    }
}
