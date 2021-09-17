/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.drive;


import java.io.Serializable;
import java.util.List;

import eu.agno3.orchestrator.system.info.storage.volume.Volume;


/**
 * @author mbechler
 * 
 */
public interface Drive extends Serializable {

    /**
     * 
     * @return drive identifier
     */
    String getId ();


    /**
     * 
     * @return drive alias
     */
    String getAssignedAlias ();


    /**
     * 
     * @return size in bytes (null if unknown)
     */
    Long getSize ();


    /**
     * 
     * @return the volumes on this drive
     */
    List<Volume> getVolumes ();


    /**
     * 
     * @return whether this is a system drive
     */
    boolean getSystem ();

}
