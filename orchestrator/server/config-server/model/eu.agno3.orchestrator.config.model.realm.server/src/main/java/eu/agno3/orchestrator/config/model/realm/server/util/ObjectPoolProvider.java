/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;


/**
 * @author mbechler
 * 
 */
public interface ObjectPoolProvider {

    /**
     * @param em
     * @param obj
     * @param type
     * @return the objects of type that are in scope at the given structural anchor
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> List<AbstractConfigurationObject<T>> getObjectsInScopeByType ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<T> type ) throws ModelServiceException;


    /**
     * 
     * @param em
     * @param obj
     * @param type
     * @return the set locally of attached objects of the given type
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> List<AbstractConfigurationObject<T>> getAttachedObjectsByType ( EntityManager em,
            AbstractStructuralObjectImpl obj, Class<T> type ) throws ModelServiceException;


    /**
     * @param em
     * @param obj
     * @param type
     * @return an locally enforced object for the type, or null if none set
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> getEnforcedForType ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<@Nullable T> type ) throws ModelServiceException;


    /**
     * @param em
     * @param obj
     * @param type
     * @return an locally default object for the type, or null if none set
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> getDefaultForType ( EntityManager em, AbstractStructuralObjectImpl obj,
            Class<T> type ) throws ModelServiceException;


    /**
     * @param em
     * @param type
     * @param at
     * @param fallback
     * @return the enforcement of type type closest in scope of at or an empty object if none exists
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> fetchInheritedEnforcement ( EntityManager em, Class<? extends T> type,
            AbstractStructuralObjectImpl at, AbstractConfigurationObject<T> fallback ) throws ModelServiceException;


    /**
     * @param em
     * @param type
     * @param at
     * @param globalDefaults
     * @return the default of type type closest in scope of at or the global defaults if none is specified
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> AbstractConfigurationObject<T> fetchInheritedDefault ( EntityManager em, Class<? extends @Nullable T> type,
            StructuralObject at, AbstractConfigurationObject<T> globalDefaults ) throws ModelServiceException;


    /**
     * @param base
     * @param toCheck
     * @return whether the object toCheck is in scope of base
     */
    boolean isInScope ( AbstractConfigurationObject<?> base, AbstractConfigurationObject<?> toCheck );


    /**
     * @param base
     * @param toCheck
     * @return whether the object toCheck is in scope of base
     */
    boolean isInScope ( AbstractStructuralObjectImpl base, AbstractConfigurationObject<?> toCheck );


    /**
     * @param em
     * @param persistentAnchor
     * @return all local and inherited enforcements
     */
    @SuppressWarnings ( "rawtypes" )
    List<AbstractConfigurationObject> getInheritedEnforcements ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor );


    /**
     * @param em
     * @param persistentAnchor
     * @return all local and inherited defaults
     */
    @SuppressWarnings ( "rawtypes" )
    List<AbstractConfigurationObject> getInheritedDefaults ( EntityManager em, AbstractStructuralObjectImpl persistentAnchor );


    /**
     * 
     * @param em
     * @param def
     * @return the objects the enforcement is directly applied to
     * @throws ModelServiceException
     */
    <T extends AbstractConfigurationObject<?>> Set<T> getEnforcementAppliedToObjects ( EntityManager em, T def ) throws ModelServiceException;


    /**
     * 
     * @param em
     * @param enforced
     * @return the structural objects to which the enforcement is applied
     * @throws ModelServiceException
     */
    Set<AbstractStructuralObjectImpl> getEnforcedAppliedTo ( EntityManager em, AbstractConfigurationObject<?> enforced ) throws ModelServiceException;


    /**
     * 
     * @param em
     * @param def
     * @return the object the default might be directly applied to
     * @throws ModelServiceException
     */
    <T extends AbstractConfigurationObject<?>> Set<T> getDefaultAppliedToObjects ( EntityManager em, T def ) throws ModelServiceException;


    /**
     * 
     * @param em
     * @param def
     * @return the structural objects to which the default might be applied
     * @throws ModelServiceException
     */
    Set<AbstractStructuralObjectImpl> getDefaultAppliedTo ( EntityManager em, AbstractConfigurationObject<?> def ) throws ModelServiceException;


    /**
     * @param em
     * @param obj
     * @return the resource libraries attach to obj
     */
    @NonNull
    List<@NonNull ResourceLibrary> getResourceLibraries ( EntityManager em, AbstractStructuralObjectImpl obj );


    /**
     * @param em
     * @param obj
     * @return the resource libraries which are available at the structural object
     */
    List<ResourceLibrary> getResourceLibrariesInScope ( EntityManager em, AbstractStructuralObjectImpl obj );


    /**
     * @param em
     * @param anchor
     * @return the types for which defaults are available
     */
    Set<Class<? extends ConfigurationObject>> getAvailableDefaultTypes ( @NonNull EntityManager em, StructuralObject anchor );


    /**
     * @param em
     * @param anchor
     * @return the types for which enforcments are available
     */
    Set<Class<? extends ConfigurationObject>> getAvailableEnforcementTypes ( @NonNull EntityManager em, StructuralObject anchor );
}