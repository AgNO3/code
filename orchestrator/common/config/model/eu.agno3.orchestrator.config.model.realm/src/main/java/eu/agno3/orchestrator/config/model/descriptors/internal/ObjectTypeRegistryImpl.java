/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors.internal;


import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * 
 */
@Component ( service = ObjectTypeRegistry.class )
public class ObjectTypeRegistryImpl implements ObjectTypeRegistry {

    private static final Logger log = Logger.getLogger(ObjectTypeRegistryImpl.class);

    private ReadWriteLock rwl = new ReentrantReadWriteLock();
    private Map<String, ObjectTypeDescriptor<@Nullable ?>> typesByName = new HashMap<>();
    private Map<String, ObjectTypeDescriptor<?>> typesByConfigClass = new HashMap<>();
    private MultiValuedMap<String, String> typesByParent = new HashSetValuedHashMap<>();
    private MultiValuedMap<String, String> typesBySuperType = new HashSetValuedHashMap<>();
    private Set<String> rootTypes = new HashSet<>();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void bindDescriptor ( ObjectTypeDescriptor<@Nullable ?> desc ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Binding descriptor for %s with class %s", //$NON-NLS-1$
                desc.getObjectTypeName(),
                desc.getObjectType().getName()));
        }

        Set<String> superTypes = new HashSet<>();
        collectSuperTypes(desc.getObjectType(), superTypes);

        Lock l = this.rwl.writeLock();
        l.lock();
        try {

            this.typesByName.put(desc.getObjectTypeName(), desc);
            this.typesByConfigClass.put(desc.getObjectType().getName(), desc);

            if ( desc.getParentTypeName() != null ) {
                this.typesByParent.put(desc.getParentTypeName(), desc.getObjectTypeName());
            }
            else {
                this.rootTypes.add(desc.getObjectTypeName());
            }

            for ( String superType : superTypes ) {
                this.typesBySuperType.put(superType, desc.getObjectTypeName());
            }
        }
        finally {
            l.unlock();
        }
    }


    /**
     * @param objectType
     * @param collectSuperTypes
     */
    private void collectSuperTypes ( @NonNull Class<@Nullable ?> objectType, Set<String> collectSuperTypes ) {
        Class<?>[] interfaces = objectType.getInterfaces();

        if ( interfaces == null ) {
            return;
        }

        for ( Class<?> intf : interfaces ) {
            if ( !ConfigurationObject.class.isAssignableFrom(intf) ) {
                continue;
            }

            ObjectTypeName name = intf.getAnnotation(ObjectTypeName.class);
            if ( name == null ) {
                continue;
            }

            if ( !collectSuperTypes.contains(name.value()) ) {
                collectSuperTypes.add(name.value());
                collectSuperTypes(intf, collectSuperTypes);
            }
        }
    }


    protected synchronized void unbindDescriptor ( ObjectTypeDescriptor<?> desc ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Unbinding descriptor for %s with class %s", //$NON-NLS-1$
                desc.getObjectTypeName(),
                desc.getObjectType().getName()));
        }

        Set<String> superTypes = new HashSet<>();
        collectSuperTypes(desc.getObjectType(), superTypes);

        Lock l = this.rwl.writeLock();
        l.lock();

        try {
            this.typesByName.remove(desc.getObjectTypeName());
            this.typesByConfigClass.remove(desc.getObjectType().getName());

            if ( desc.getParentTypeName() != null ) {
                this.typesByParent.removeMapping(desc.getParentTypeName(), desc.getObjectTypeName());
            }
            else {
                this.rootTypes.remove(desc.getObjectTypeName());
            }

            for ( String superType : superTypes ) {
                this.typesBySuperType.removeMapping(superType, desc.getObjectTypeName());
            }
        }
        finally {
            l.unlock();
        }
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.String)
     */
    @Override
    public ObjectTypeDescriptor<@Nullable ?> get ( String type ) throws ModelServiceException {
        ObjectTypeDescriptor<@Nullable ?> descriptor = this.typesByName.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No object type found for name " + type); //$NON-NLS-1$
        }

        return descriptor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry#getConcrete(java.lang.String)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull ConcreteObjectTypeDescriptor<@Nullable ?, @Nullable ? extends AbstractConfigurationObject<?>> getConcrete ( String type )
            throws ModelServiceException {
        ObjectTypeDescriptor<@Nullable ?> descriptor = this.typesByName.get(type);

        if ( descriptor == null ) {
            throw new ModelServiceException("No object type found for name " + type); //$NON-NLS-1$
        }

        if ( descriptor.isAbstract() || ! ( descriptor instanceof ConcreteObjectTypeDescriptor ) ) {
            throw new ModelServiceException("Object type is abstract " + type); //$NON-NLS-1$
        }

        return (ConcreteObjectTypeDescriptor<@Nullable ?, @Nullable ? extends AbstractConfigurationObject<?>>) descriptor;
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getDescriptor(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> ObjectTypeDescriptor<T> get ( Class<T> type ) throws ModelServiceException {
        ObjectTypeDescriptor<?> descriptor = this.typesByConfigClass.get(type.getName());

        if ( descriptor == null ) {
            throw new ModelServiceException("Unknown object type " + type.getName()); //$NON-NLS-1$
        }

        if ( !descriptor.getObjectType().equals(type) ) {
            throw new ModelServiceException("Incompatible object type class " + type.getName()); //$NON-NLS-1$
        }

        return (ObjectTypeDescriptor<T>) descriptor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry#getConcrete(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> ConcreteObjectTypeDescriptor<T, ? extends AbstractConfigurationObject<?>> getConcrete (
            Class<? extends T> type ) throws ModelServiceException {
        ObjectTypeDescriptor<?> descriptor = this.typesByConfigClass.get(type.getName());

        if ( descriptor == null ) {
            throw new ModelServiceException("Unknown object type " + type.getName()); //$NON-NLS-1$
        }

        if ( !descriptor.getObjectType().equals(type) ) {
            throw new ModelServiceException("Incompatible object type class " + type.getName()); //$NON-NLS-1$
        }

        if ( descriptor.isAbstract() || ! ( descriptor instanceof ConcreteObjectTypeDescriptor ) ) {
            throw new ModelServiceException("Object type is abstract " + type); //$NON-NLS-1$
        }

        return (ConcreteObjectTypeDescriptor<T, ? extends AbstractConfigurationObject<?>>) descriptor;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry#getByParent(java.lang.Class)
     */
    @Override
    public Set<ObjectTypeDescriptor<?>> getByParent ( Class<? extends ConfigurationObject> parent ) throws ModelServiceException {
        Set<ObjectTypeDescriptor<?>> descriptors = new HashSet<>();
        ObjectTypeDescriptor<? extends ConfigurationObject> parentTypeDescriptor = null;
        if ( parent != null ) {
            parentTypeDescriptor = this.get(parent);
        }
        Lock l = this.rwl.readLock();
        l.lock();

        try {
            Collection<String> types;

            if ( parentTypeDescriptor == null ) {
                types = this.rootTypes;
            }
            else {
                types = this.typesByParent.get(parentTypeDescriptor.getObjectTypeName());
            }

            if ( types != null ) {
                for ( String type : types ) {
                    descriptors.add(this.get(type));
                }
            }
        }
        finally {
            l.unlock();
        }

        return descriptors;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry#getConcreteByParent(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public Set<ConcreteObjectTypeDescriptor<?, ?>> getConcreteByParent ( Class<? extends ConfigurationObject> parent ) throws ModelServiceException {
        Set<ConcreteObjectTypeDescriptor<?, ?>> descriptors = new HashSet<>();
        ObjectTypeDescriptor<? extends ConfigurationObject> parentTypeDescriptor = null;
        if ( parent != null ) {
            parentTypeDescriptor = this.get(parent);
        }
        Lock l = this.rwl.readLock();
        l.lock();

        try {
            Collection<String> types;

            if ( parentTypeDescriptor == null ) {
                types = this.rootTypes;
            }
            else {
                types = this.typesByParent.get(parentTypeDescriptor.getObjectTypeName());
            }

            if ( types != null ) {
                for ( String type : types ) {
                    ObjectTypeDescriptor<@Nullable ?> objectTypeDescriptor = this.get(type);
                    if ( !objectTypeDescriptor.isAbstract() && objectTypeDescriptor instanceof ConcreteObjectTypeDescriptor ) {
                        descriptors
                                .add((ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>) objectTypeDescriptor);
                    }
                }
            }
        }
        finally {
            l.unlock();
        }

        return descriptors;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ModelServiceException
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry#getApplicableTypes(java.lang.String)
     */
    @Override
    public Set<ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>> getApplicableTypes (
            String objectType ) throws ModelServiceException {
        Set<ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>> res = new HashSet<>();
        addChildren(objectType, res);
        return res;
    }


    /**
     * @param objectType
     * @param res
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "unchecked" )
    private void addChildren ( String objectType,
            Set<ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>> res )
            throws ModelServiceException {

        Collection<String> children = this.typesBySuperType.get(objectType);
        ObjectTypeDescriptor<@Nullable ?> objectTypeDescriptor = this.get(objectType);

        if ( !objectTypeDescriptor.isAbstract() && objectTypeDescriptor instanceof ConcreteObjectTypeDescriptor ) {
            res.add((ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>) objectTypeDescriptor);
        }

        if ( children == null ) {
            return;
        }

        for ( String childType : children ) {
            addChildren(childType, res);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry#getServiceTypes()
     */
    @Override
    public Set<String> getObjectTypes () {
        return this.typesByName.keySet();
    }
}
