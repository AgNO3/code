/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.util.UUID;


/**
 * @author mbechler
 *
 */
public interface LocalMountEntryMutable extends LocalMountEntry, MountEntryMutable {

    /**
     * 
     * @param matchLabel
     */
    void setMatchLabel ( String matchLabel );


    /**
     * 
     * @param matchUuid
     */
    void setMatchUuid ( UUID matchUuid );

}
