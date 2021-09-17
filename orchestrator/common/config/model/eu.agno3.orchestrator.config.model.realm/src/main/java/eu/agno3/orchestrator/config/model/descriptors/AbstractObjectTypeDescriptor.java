/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * @param <T>
 * @param <TImpl>
 * 
 */
public abstract class AbstractObjectTypeDescriptor <T extends ConfigurationObject, TImpl extends AbstractConfigurationObject<T>>
        implements ConcreteObjectTypeDescriptor<T, TImpl> {

    private final @NonNull String typeName;
    private final @NonNull Class<T> objectType;
    private final @NonNull Class<TImpl> implType;
    private final String parentTypeName;
    private final String localizationBase;


    protected AbstractObjectTypeDescriptor ( @NonNull Class<T> objectType, @NonNull Class<TImpl> implType, String localizationBase ) {
        this(objectType, implType, localizationBase, null);
    }


    protected AbstractObjectTypeDescriptor ( @NonNull Class<T> objectType, @NonNull Class<TImpl> implType, String localizationBase,
            String parentTypeName ) {
        this.objectType = objectType;
        this.parentTypeName = parentTypeName;
        this.implType = implType;
        ObjectTypeName annot = objectType.getAnnotation(ObjectTypeName.class);

        if ( annot == null ) {
            throw new ObjectTypeDescriptorException(String.format("Object type class %s does not declare a ObjectTypeName", objectType.getName())); //$NON-NLS-1$
        }
        String value = annot.value();
        if ( value == null || value.isEmpty() ) {
            throw new ObjectTypeDescriptorException("ObjectTypeName is empty"); //$NON-NLS-1$
        }
        this.typeName = value;
        this.localizationBase = localizationBase;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ObjectTypeDescriptor#isAbstract()
     */
    @Override
    public boolean isAbstract () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#hasOverrideDefaultsFor(java.lang.Class)
     */
    @Override
    public boolean hasOverrideDefaultsFor ( @NonNull Class<? extends @Nullable ConfigurationObject> type ) {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getOverrideDefaults(java.lang.Class)
     */
    @Override
    public <@Nullable TOverride extends ConfigurationObject> @Nullable TOverride getOverrideDefaults ( @NonNull Class<? extends TOverride> type ) {
        return null;
    }


    /**
     * @return the implType
     */
    @Override
    public @NonNull Class<TImpl> getImplementationType () {
        return this.implType;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#isHidden()
     */
    @Override
    public boolean isHidden () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getLocalizationBase()
     */
    @Override
    public String getLocalizationBase () {
        return this.localizationBase;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getObjectType()
     */
    @SuppressWarnings ( "null" )
    @Override
    public @NonNull Class<@Nullable T> getObjectType () {
        return this.objectType;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getParentTypeName()
     */
    @Override
    public String getParentTypeName () {
        return this.parentTypeName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getObjectTypeName()
     */
    @Override
    public String getObjectTypeName () {
        return this.typeName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#getGlobalDefaults()
     */
    @Override
    public @NonNull T getGlobalDefaults () {
        return this.newInstance();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.config.model.descriptors.ConcreteObjectTypeDescriptor#newInstance()
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public @NonNull T newInstance () {
        try {
            T obj = (T) this.implType.newInstance();

            if ( obj == null ) {
                throw new ObjectTypeDescriptorException("Created object is null"); //$NON-NLS-1$
            }

            return obj;
        }
        catch (
            InstantiationException |
            IllegalAccessException e ) {
            throw new ObjectTypeDescriptorException("Implementation type cannot be instantiated", e); //$NON-NLS-1$
        }
    }
}
