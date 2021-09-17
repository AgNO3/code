/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;


/**
 * @author mbechler
 * 
 */
public interface DefaultsServerService {

    /**
     * 
     * @param cc
     * @param persistent
     * @return the defaults applied to the given object
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> T getAppliedDefaults ( InheritanceProxyContext cc, AbstractConfigurationObject<@Nullable T> persistent )
            throws ModelServiceException;


    /**
     * 
     * @param cc
     * @param obj
     * @param objType
     * @return the defaults for the given object type at the given structural node
     * @throws ModelServiceException
     */
    ConfigurationObject getDefaultsFor ( InheritanceProxyContext cc, AbstractStructuralObjectImpl obj, String objType ) throws ModelServiceException;

}