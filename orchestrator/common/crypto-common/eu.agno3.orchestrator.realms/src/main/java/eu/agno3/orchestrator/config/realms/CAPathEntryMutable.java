/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


/**
 * @author mbechler
 *
 */
public interface CAPathEntryMutable {

    /**
     * @param targetRealm
     *            the targetRealm to set
     */
    void setTargetRealm ( String targetRealm );


    /**
     * @param nextRealm
     *            the nextRealm to set
     */
    void setNextRealm ( String nextRealm );

}