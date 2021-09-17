/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectConflictException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectValidationException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigUpdateInfo;


/**
 * @author mbechler
 * 
 */
public interface ConfigurationServerService {

    /**
     * 
     * @param em
     * @param anchor
     * @param create
     * @param info
     * @return the created object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     */
    <T extends ConfigurationObject> T create ( EntityManager em, AbstractStructuralObjectImpl anchor, T create, @Nullable ConfigUpdateInfo info )
            throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException;


    /**
     * @param em
     * @param persistentObj
     * @param update
     * @param info
     * @return the updated object
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     * @throws ModelObjectValidationException
     * @throws ModelObjectConflictException
     */
    <T extends ConfigurationObject> T update ( EntityManager em, AbstractConfigurationObject<T> persistentObj, T update,
            @Nullable ConfigUpdateInfo info ) throws ModelServiceException, ModelObjectNotFoundException, ModelObjectValidationException,
            ModelObjectConflictException;


    /**
     * @param objectType
     * @return an empty object
     * @throws ModelServiceException
     */
    @Nullable
    ConfigurationObject getEmpty ( @Nullable String objectType ) throws ModelServiceException;

}