/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.Set;

import javax.persistence.EntityManager;

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
public interface EnforcementServerService {

    /**
     * 
     * @param em
     * @param persistent
     * @return the enforcements attached to the given anchor
     */
    Set<ConfigurationObject> fetchEnforcements ( EntityManager em, AbstractStructuralObjectImpl persistent );


    /**
     * 
     * @param cc
     * @param persistent
     * @return the enforment applied to the given object
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> T getAppliedEnforcement ( InheritanceProxyContext cc, AbstractConfigurationObject<@Nullable T> persistent )
            throws ModelServiceException;


    /**
     * 
     * @param cc
     * @param em
     * @param persistent
     * @param objType
     * @return the enforcement applied to the specified object type at the given anchor
     * @throws ModelServiceException
     */
    ConfigurationObject getEnforcementsFor ( InheritanceProxyContext cc, AbstractStructuralObjectImpl persistent, String objType )
            throws ModelServiceException;

}