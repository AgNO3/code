/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.system.info.storage.volume.LogicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.PhysicalVolume;
import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * Sorts volumes for displaying
 * 
 * Logical volumes are compared by name, physical volumes by their index
 * 
 * @author mbechler
 * 
 */
final class VolumeDisplayComparator implements Comparator<Volume>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5818352600646976211L;


    /**
     * 
     */
    public VolumeDisplayComparator () {}


    @Override
    public int compare ( Volume o1, Volume o2 ) {
        if ( o1 instanceof LogicalVolume && o2 instanceof LogicalVolume ) {
            return ( (LogicalVolume) o1 ).getName().compareTo( ( (LogicalVolume) o2 ).getName());
        }
        if ( o1 instanceof PhysicalVolume && o2 instanceof PhysicalVolume ) {
            return Integer.compare( ( (PhysicalVolume) o1 ).getIndex(), ( (PhysicalVolume) o2 ).getIndex());
        }
        return 0;
    }
}