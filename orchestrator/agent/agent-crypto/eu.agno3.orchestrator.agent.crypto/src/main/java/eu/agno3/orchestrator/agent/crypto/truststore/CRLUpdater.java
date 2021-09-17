/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.crypto.truststore;




/**
 * @author mbechler
 *
 */
public interface CRLUpdater {

    /**
     * @param tm
     * @throws TruststoreManagerException
     */
    void updateCRLsFromDistributionPoints ( TruststoreManager tm ) throws TruststoreManagerException;

}