/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractBaseObjectTypeDescriptor <T extends ConfigurationObject> implements ObjectTypeDescriptor<T> {

    private final @NonNull String typeName;
    private final @NonNull Class<T> objectType;
    private final String parentTypeName;
    private final String localizationBase;


    protected AbstractBaseObjectTypeDescriptor ( @NonNull Class<T> objectType, String localizationBase ) {
        this(objectType, localizationBase, null);
    }


    protected AbstractBaseObjectTypeDescriptor ( @NonNull Class<T> objectType, String localizationBase, String parentTypeName ) {
        this.objectType = objectType;
        this.parentTypeName = parentTypeName;
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
        return true;
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

}
