/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.license.LicenseStorage;


/**
 * @author mbechler
 * 
 */
public interface InstanceStructuralObject extends StructuralObject {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.realm.StructuralObject#getDisplayName()
     */
    @Override
    @NotNull
    @Size ( min = 1, max = 80 )
    String getDisplayName ();


    /**
     * @return the agentId
     */
    UUID getAgentId ();


    /**
     * @return the image type of this instance
     */
    @NotNull
    String getImageType ();


    /**
     * @return the release stream for this object
     */
    String getReleaseStream ();


    /**
     * @return the license assigned to this instance
     */
    LicenseStorage getAssignedLicense ();


    /**
     * @return the time the demo expires
     */
    DateTime getDemoExpiration ();

}