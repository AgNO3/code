/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.versioning;


import java.io.Serializable;
import java.util.Date;


/**
 * @author mbechler
 * 
 */
public interface VersionInfo extends Serializable {

    /**
     * @return the revision number
     */
    long getRevisionNumber ();


    /**
     * @return the time the revision was created
     */
    Date getRevisionTime ();


    /**
     * @return the type of revision
     */
    RevisionType getRevisionType ();

}
