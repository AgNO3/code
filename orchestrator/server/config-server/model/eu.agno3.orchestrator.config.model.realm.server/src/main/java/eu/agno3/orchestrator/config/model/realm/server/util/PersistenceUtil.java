/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


import java.util.Date;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.exception.RevisionDoesNotExistException;
import org.hibernate.proxy.HibernateProxy;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.BaseObject;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.base.exceptions.faults.ModelObjectNotFoundFault;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObjectReference;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;


/**
 * @author mbechler
 * 
 */
@Component ( service = PersistenceUtil.class )
public class PersistenceUtil {

    private static final Logger log = Logger.getLogger(PersistenceUtil.class);

    private static final String DEFAULT_FAILURE_STRING = "Failed to fetch object"; //$NON-NLS-1$

    private ObjectTypeRegistry objectTypeRegistry;


    @Reference
    protected synchronized void setObjectTypeRegistry ( ObjectTypeRegistry reg ) {
        this.objectTypeRegistry = reg;
    }


    protected synchronized void unsetObjectTypeRegistry ( ObjectTypeRegistry reg ) {
        if ( this.objectTypeRegistry == reg ) {
            this.objectTypeRegistry = null;
        }
    }


    /**
     * @param em
     * @param obj
     * @return the persistent object for the object identified by obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    public <P extends ConfigurationObject> @NonNull AbstractConfigurationObject<P> fetch ( @NonNull EntityManager em, @Nullable P obj )
            throws ModelObjectNotFoundException, ModelServiceException {
        return fetch(em, obj, DEFAULT_FAILURE_STRING);
    }


    /**
     * @param em
     * @param obj
     * @param failure
     * @return the persistent object for the object identified by obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    @SuppressWarnings ( {
        "unchecked"
    } )
    public <P extends ConfigurationObject> @NonNull AbstractConfigurationObject<P> fetch ( @NonNull EntityManager em, @Nullable P obj,
            String failure ) throws ModelObjectNotFoundException, ModelServiceException {

        if ( obj == null ) {
            throw new ModelObjectNotFoundException(new ModelObjectNotFoundFault());
        }

        validateReference(obj);

        AbstractConfigurationObject<? extends ConfigurationObject> persistent;
        if ( obj instanceof ConfigurationObjectReference ) {
            persistent = em.find(
                this.objectTypeRegistry.getConcrete( ( (ConfigurationObjectReference) obj ).getObjectTypeName()).getImplementationType(),
                obj.getId());
        }
        else {
            @NonNull
            Class<?> implementationType = this.objectTypeRegistry.getConcrete(obj.getType()).getImplementationType();
            persistent = (AbstractConfigurationObject<? extends ConfigurationObject>) em.find(implementationType, obj.getId());
        }

        if ( persistent == null ) {
            throw new ModelObjectNotFoundException(obj.getType(), obj.getId());
        }

        validatePersistent(obj, failure, persistent);
        return (AbstractConfigurationObject<P>) persistent;
    }


    /**
     * @param em
     * @param persistenceClass
     * @param id
     * @param failure
     * @return the object of the given type and id
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    public static <P extends BaseObject> @NonNull P fetch ( EntityManager em, Class<P> persistenceClass, UUID id, String failure )
            throws ModelObjectNotFoundException, ModelServiceException {
        P persistent = em.find(persistenceClass, id);
        if ( persistent == null ) {
            throw new ModelObjectNotFoundException(failure, new ModelObjectNotFoundFault(persistenceClass, id));
        }
        return persistent;
    }


    /**
     * 
     * @param em
     * @param persistenceClass
     * @param id
     * @return the object of the given type and id
     * @throws ModelServiceException
     * @throws ModelObjectNotFoundException
     */
    public static <P extends BaseObject> @NonNull P fetch ( EntityManager em, Class<P> persistenceClass, UUID id )
            throws ModelObjectNotFoundException, ModelServiceException {
        return fetch(em, persistenceClass, id, DEFAULT_FAILURE_STRING);
    }


    /**
     * @param em
     * @param persistenceClass
     * @param obj
     * @return the persistent object for the object identified by obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    public <P extends StructuralObject> @NonNull P fetch ( EntityManager em, Class<P> persistenceClass, StructuralObject obj )
            throws ModelObjectNotFoundException, ModelServiceException {
        return fetch(em, persistenceClass, obj, DEFAULT_FAILURE_STRING);
    }


    /**
     * @param em
     * @param persistenceClass
     * @param obj
     * @param failure
     * @return the persistent object for the object identified by obj
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    public <P extends StructuralObject> @NonNull P fetch ( EntityManager em, Class<P> persistenceClass, StructuralObject obj, String failure )
            throws ModelObjectNotFoundException, ModelServiceException {
        validateReference(obj);
        P persistent = em.find(persistenceClass, obj.getId());
        validatePersistent(obj, failure, persistent);

        if ( persistent == null ) {
            throw new ModelObjectNotFoundException(StructuralObject.class, obj.getId());
        }

        return persistent;
    }


    /**
     * 
     * @param em
     * @param obj
     * @param rev
     * @return the modified passed object graph
     * @throws ModelServiceException
     */
    public <T extends AbstractConfigurationObject<?>> @NonNull T setRevisions ( @NonNull EntityManager em, @NonNull T obj, long rev )
            throws ModelServiceException {
        try {
            ReferenceWalker.walk(
                new PersistentReferenceWalkerContext(em, this),
                obj,
                new RecursiveReferenceVisitor(new RevisionSettingReferenceVisitor(rev)));
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Unexpected exception when setting revisions", e); //$NON-NLS-1$
        }
        return obj;
    }


    /**
     * @param em
     * @return the most recent revision in the persistence unit
     * @throws ModelServiceException
     */
    public static long getMostRecentRevision ( EntityManager em ) throws ModelServiceException {

        AuditReader ar = AuditReaderFactory.get(em);

        Long rev = null;
        try {
            rev = (Long) ar.getRevisionNumberForDate(new Date());
        }
        catch ( RevisionDoesNotExistException e ) {
            log.warn("Failed to get revision number", e); //$NON-NLS-1$
        }

        if ( rev == null ) {
            throw new ModelServiceException("Could not determine current revision number"); //$NON-NLS-1$
        }

        return rev;
    }


    /**
     * @param object
     * @return an unproxied object graph (unproxy the object and all referenced objects)
     * @throws ModelServiceException
     */
    public static <T extends AbstractConfigurationObject<?>> @NonNull T unproxyDeep ( T object ) throws ModelServiceException {

        if ( object == null ) {
            throw new IllegalArgumentException();
        }

        @Nullable
        T obj = unproxy(object);

        if ( log.isTraceEnabled() ) {
            log.trace("Unproxied " + object); //$NON-NLS-1$
        }

        try {
            ReferenceWalker.walkReferences(new EmptyReferenceWalkerContext(), object, new UnproxyReplacementHandler());
        }
        catch ( ModelObjectException e ) {
            throw new ModelServiceException("Unexpected error during unproxying", e); //$NON-NLS-1$
        }

        return obj;
    }


    private static <T extends BaseObject> void validateReference ( @Nullable T obj ) throws ModelServiceException {

        if ( obj == null || obj.getId() == null ) {
            throw new ModelServiceException("Object reference is invalid (null)"); //$NON-NLS-1$
        }

    }


    private static void validatePersistent ( BaseObject obj, String failure, BaseObject persistent ) throws ModelObjectNotFoundException {
        if ( persistent == null ) {
            throw new ModelObjectNotFoundException(failure, new ModelObjectNotFoundFault(obj.getClass(), obj.getId()));
        }
    }


    /**
     * Removes a hibernate proxy and loads the entity
     * 
     * @param entity
     * @return the entity instance
     */
    @SuppressWarnings ( {
        "unchecked", "null"
    } )
    public static <T> @NonNull T unproxy ( @NonNull T entity ) {

        if ( entity instanceof HibernateProxy ) {
            return (T) ( (HibernateProxy) entity ).getHibernateLazyInitializer().getImplementation();
        }
        return entity;
    }
}
