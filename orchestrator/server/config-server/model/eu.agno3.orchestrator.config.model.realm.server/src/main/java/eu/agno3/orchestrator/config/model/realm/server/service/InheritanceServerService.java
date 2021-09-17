/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.service;


import java.util.List;

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
public interface InheritanceServerService {

    /**
     * 
     * @param cc
     * @param persistent
     * @param rootType
     * @return the inherited values for the given object
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> T getInherited ( InheritanceProxyContext cc, AbstractConfigurationObject<@Nullable T> persistent )
            throws ModelServiceException;


    /**
     * 
     * @param cc
     * @param persistent
     * @param rootType
     * @return the effective values for the given object
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> T getEffective ( InheritanceProxyContext cc, AbstractConfigurationObject<@Nullable T> persistent )
            throws ModelServiceException;


    /**
     * @param em
     * @param persistent
     * @param rootType
     * @return the effective values for the given object
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> T getEffective ( EntityManager em, AbstractConfigurationObject<@Nullable T> persistent,
            @Nullable Class<? extends ConfigurationObject> rootType ) throws ModelServiceException;


    /**
     * 
     * @param em
     * @param persistentAnchor
     * @param objType
     * @return the templates that are in scope for the given object type at the given anchor
     * @throws ModelServiceException
     */
    List<ConfigurationObject> getEligibleTemplates ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor, String objType )
            throws ModelServiceException;

}