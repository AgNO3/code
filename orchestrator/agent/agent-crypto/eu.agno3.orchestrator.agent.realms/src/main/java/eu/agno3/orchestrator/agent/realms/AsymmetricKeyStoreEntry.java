/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.security.PublicKey;


/**
 * @author mbechler
 *
 */
public interface AsymmetricKeyStoreEntry extends KeyStoreEntry {

    /**
     * @return the public key
     */
    PublicKey getPublicKey ();

}
