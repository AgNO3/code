/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.security.cert.Certificate;


/**
 * @author mbechler
 *
 */
public interface KeyStoreEntry {

    /**
     * 
     * @return the key alias
     */
    String getAlias ();


    /**
     * 
     * @return the key type/size
     */
    String getType ();


    /**
     * 
     * @return associated certificate chain
     */
    Certificate[] getCertificateChain ();
}
