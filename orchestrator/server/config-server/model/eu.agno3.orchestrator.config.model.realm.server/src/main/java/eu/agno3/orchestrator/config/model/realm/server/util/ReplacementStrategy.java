/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * A strategy for handling object references
 * 
 * @author mbechler
 * 
 */
public interface ReplacementStrategy {

    /**
     * Called for each found reference if it's value is not yet persistent
     * 
     * @param em
     * @param pu
     * @param enclosing
     * @param refObj
     * @param outerRef
     * @param collection
     * @return the replacement object
     * @throws ModelObjectException
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> handleUnpersistedRef ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractConfigurationObject<?> enclosing, T refObj, T outerRef, boolean collection ) throws ModelObjectException, ModelServiceException;


    /**
     * Called for each found reference if it'S value is already persistent
     * 
     * @param em
     * @param pu
     * @param enclosing
     * @param refObj
     * @param outerRef
     * @param collection
     * @return the replacement object
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> handlePersistedRef ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractConfigurationObject<?> enclosing, T refObj, T outerRef, boolean collection ) throws ModelObjectException, ModelServiceException;
}