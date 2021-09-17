/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;



/**
 * @author mbechler
 * 
 */
public interface RAIDDrive extends Drive {

    /**
     * @return this arrays RAID level
     */
    RAIDLevel getRaidLevel ();


    /**
     * @return total number of disks in this array
     */
    int getNumDevices ();


    /**
     * 
     * @return number of drives missing for a fully working array
     */
    int getNumDegraded ();


    /**
     * 
     * @return number of available spare drives
     */
    int getNumSpares ();

}
