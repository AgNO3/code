/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import java.util.Optional;

import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.ViolationLevel;


/**
 * @author mbechler
 *
 */
public interface ObjectValidationContext {

    /**
     * @return whether errors occured during validation
     */
    boolean hasErrors ();


    /**
     * 
     * @param level
     * @param msgKey
     * @param msgArgs
     */
    void addViolation ( String msgKey, ViolationLevel level, Object... msgArgs );


    /**
     * @param msgKey
     * @param objectPath
     * @param level
     * @param msgArgs
     */
    void addViolation ( String msgKey, String objectPath, ViolationLevel level, Object... msgArgs );


    /**
     * @return whether this is a template or instance
     */
    boolean isAbstract ();


    /**
     * @param type
     * @return an in-context object of the given type
     */
    <T extends ConfigurationObject> Optional<T> findParent ( Class<T> type );


    /**
     * 
     * @param type
     * @param service
     * @return an in-context service configuration of the given type
     */
    <T extends ConfigurationInstance> Optional<T> findContext ( Class<T> type, String service );

}
