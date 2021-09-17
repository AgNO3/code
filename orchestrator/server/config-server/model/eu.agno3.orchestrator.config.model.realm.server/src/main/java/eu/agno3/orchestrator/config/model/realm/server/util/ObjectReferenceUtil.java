/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationInstance;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.AbstractStructuralObjectImpl;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public final class ObjectReferenceUtil {

    private static final Logger log = Logger.getLogger(ObjectReferenceUtil.class);


    /**
     * 
     */
    private ObjectReferenceUtil () {}


    /**
     * Replaces null references by empty objects
     * 
     * @param em
     * @param pu
     * @param obj
     * @param reg
     * @throws ModelServiceException
     */
    public static <T extends ConfigurationObject> void fillInMissing ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractConfigurationObject<T> obj, ObjectTypeRegistry reg ) throws ModelServiceException {
        try {
            ReferenceWalker.walk(new PersistentReferenceWalkerContext(em, pu), obj, new AddMissingReferenceHandler(reg));
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Failed to fill in missing references", e); //$NON-NLS-1$
        }
    }


    /**
     * @param em
     * @param pu
     * @param persistentAnchor
     * @param persistentObj
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public static <T extends ConfigurationObject> void prepareObjectForPersist ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractStructuralObjectImpl persistentAnchor, AbstractConfigurationObject<T> persistentObj )
                    throws ModelServiceException, ModelObjectNotFoundException {
        persistentAnchor.addObject(persistentObj);
        persistentObj.setId(null);

        if ( persistentObj.getInherits() != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Setting inherits to persistent " + persistentObj.getInherits()); //$NON-NLS-1$
            }
            AbstractConfigurationObject<?> persistentInherits = pu.fetch(em, persistentObj.getInherits());
            persistentObj.setInherits(persistentInherits);
            persistentInherits.getInheritedBy().add(persistentObj);
        }

        try {
            ObjectReferenceUtil.replaceReferences(em, pu, new PersistStrategy(), persistentObj);
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Failed to prepare referenced objects for persistence", e); //$NON-NLS-1$
        }
        UsageUtil.updateUsage(persistentObj);
    }


    /**
     * @param em
     * @param pu
     * @param persistentObj
     * @param update
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public static <T extends ConfigurationObject> void prepareObjectForMerge ( @NonNull EntityManager em, @NonNull PersistenceUtil pu,
            AbstractConfigurationObject<T> persistentObj, T update ) throws ModelServiceException, ModelObjectNotFoundException {

        AbstractConfigurationObject<?> updateObj = (AbstractConfigurationObject<?>) update;

        // add necessary references that are not remotely transferred
        updateObj.setAnchor(persistentObj.getAnchor());
        // uses/usedBy is required for the following updateUsage call to work properly
        log.debug("Cloning uses"); //$NON-NLS-1$
        updateObj.setUses(persistentObj.getUses());
        updateObj.setUsedBy(persistentObj.getUsedBy());
        // subtemplate/outerObject relation
        updateObj.setOuterObject(persistentObj.getOuterObject());
        updateObj.setSubObjects(persistentObj.getSubObjects());

        if ( updateObj instanceof AbstractConfigurationInstance<?> ) {
            AbstractConfigurationInstance<?> instance = (AbstractConfigurationInstance<?>) persistentObj;
            ( (AbstractConfigurationInstance<?>) updateObj ).setForService(instance.getForService());
        }

        // replace inherits with persistent object
        if ( updateObj.getInherits() != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Setting inherits to " + updateObj.getInherits()); //$NON-NLS-1$
            }
            AbstractConfigurationObject<?> persistentInherits = pu.fetch(em, updateObj.getInherits());
            updateObj.setInherits(persistentInherits);
            persistentInherits.getInheritedBy().remove(updateObj);
            persistentInherits.getInheritedBy().add(updateObj);
        }
        else if ( persistentObj.getInherits() != null ) {
            log.debug("Removing inherits pointer"); //$NON-NLS-1$
            ( (AbstractConfigurationObject<?>) persistentObj.getInherits() ).getInheritedBy().remove(persistentObj);
        }
        else {
            log.debug("No inheritance specified"); //$NON-NLS-1$
        }

        try {
            ObjectReferenceUtil.replaceReferences(em, pu, new MergeStrategy(), updateObj);
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Failed to prepare referenced objects for merge", e); //$NON-NLS-1$
        }

        UsageUtil.updateUsage(updateObj);
    }


    /**
     * @param em
     * @param pu
     * @param strategy
     * @param obj
     * @throws ModelServiceException
     * @throws ModelObjectException
     */
    public static void replaceReferences ( @NonNull EntityManager em, @NonNull PersistenceUtil pu, @NonNull ReplacementStrategy strategy,
            AbstractConfigurationObject<?> obj ) throws ModelServiceException, ModelObjectException {
        if ( log.isDebugEnabled() ) {
            log.debug("Replacing references in " + obj); //$NON-NLS-1$
        }

        if ( obj.getInherits() != null ) {
            obj.setInherits(pu.fetch(em, obj.getInherits()));
        }

        ReferenceWalker.walkReferences(new PersistentReferenceWalkerContext(em, pu), obj, new ReferenceReplacementHandler(em, pu, strategy));
    }

}
