/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import javax.validation.constraints.NotNull;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;


/**
 * @author mbechler
 * 
 */
public interface ServiceStructuralObject extends StructuralObject {

    /**
     * 
     * @return the attached configuration instance
     */
    ConfigurationInstance getConfiguration ();


    /**
     * 
     * @return the type of the service
     */
    @NotNull
    String getServiceType ();


    /**
     * @return the service configuration state
     */
    ConfigurationState getState ();


    /**
     * @return the config revision current applied
     */
    Long getAppliedRevision ();

}
