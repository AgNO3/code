/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( MountEntry.class )
public interface MountEntryMutable extends MountEntry {

    /**
     * 
     * @param alias
     */
    void setAlias ( String alias );

}
