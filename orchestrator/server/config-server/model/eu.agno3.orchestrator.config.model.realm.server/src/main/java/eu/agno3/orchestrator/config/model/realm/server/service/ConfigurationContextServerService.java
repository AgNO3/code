/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.context.ConfigurationEditContext;
import eu.agno3.orchestrator.config.model.realm.server.util.InheritanceProxyContext;


/**
 * @author mbechler
 * 
 */
public interface ConfigurationContextServerService {

    /**
     * @param cc
     * @param anchor
     * @param config
     * @param state
     * @param inner
     * @return an edit context for the given (typically not yet persisted) config in the context of anchor
     * @throws ModelServiceException
     */
    ConfigurationEditContext<ConfigurationObject, ConfigurationObject> getContextAtAnchor ( InheritanceProxyContext cc,
            AbstractStructuralObjectImpl anchor, ConfigurationObject config, ConfigurationState state, boolean inner ) throws ModelServiceException;


    /**
     * @param cc
     * @param config
     * @param state
     * @return an edit context for the given persistent config
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    ConfigurationEditContext<ConfigurationObject, ConfigurationObject> getContextForConfig ( InheritanceProxyContext cc,
            AbstractConfigurationObject<@Nullable ?> config, ConfigurationState state ) throws ModelObjectNotFoundException, ModelServiceException;

}