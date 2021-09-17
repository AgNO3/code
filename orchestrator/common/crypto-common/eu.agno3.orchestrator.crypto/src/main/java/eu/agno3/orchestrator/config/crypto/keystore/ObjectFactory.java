/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.11.2014 by mbechler
 */
package eu.agno3.orchestrator.config.crypto.keystore;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public KeystoresConfig createKeystoresConfig () {
        return new KeystoresConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public KeystoreConfig createKeystoreConfig () {
        return new KeystoreConfigImpl();
    }


    /**
     * 
     * @return default impl
     */
    public ImportKeyPairEntry createImportKeyPairEntry () {
        return new ImportKeyPairEntryImpl();
    }
}
