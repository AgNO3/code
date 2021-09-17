/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.system.info.storage.drive.Drive;
import eu.agno3.orchestrator.system.info.storage.drive.RAIDDrive;
import eu.agno3.orchestrator.system.info.storage.drive.VolumeGroup;


/**
 * Sorts drives for displaying in lists
 * 
 * VolumeGroup before RAIDs before physical Disks.
 * Sorted by drive ID.
 * 
 * @author mbechler
 * 
 */
final class DriveDisplayComparator implements Comparator<Drive>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2385049819580403562L;


    /**
     * 
     */
    public DriveDisplayComparator () {}


    @Override
    public int compare ( Drive o1, Drive o2 ) {

        if ( volBeforeOther(o1, o2) ) {
            return -1;
        }
        else if ( otherAfterVol(o1, o2) ) {
            return 1;
        }

        if ( raidBeforeOther(o1, o2) ) {
            return -1;
        }
        else if ( otherAfterRaid(o1, o2) ) {
            return 1;
        }

        return o1.getId().compareTo(o2.getId());
    }


    private static boolean otherAfterRaid ( Drive o1, Drive o2 ) {
        return ! ( o1 instanceof RAIDDrive ) && o2 instanceof RAIDDrive;
    }


    private static boolean raidBeforeOther ( Drive o1, Drive o2 ) {
        return o1 instanceof RAIDDrive && ! ( o2 instanceof RAIDDrive );
    }


    private static boolean otherAfterVol ( Drive o1, Drive o2 ) {
        return ! ( o1 instanceof VolumeGroup ) && o2 instanceof VolumeGroup;
    }


    private static boolean volBeforeOther ( Drive o1, Drive o2 ) {
        return o1 instanceof VolumeGroup && ! ( o2 instanceof VolumeGroup );
    }
}