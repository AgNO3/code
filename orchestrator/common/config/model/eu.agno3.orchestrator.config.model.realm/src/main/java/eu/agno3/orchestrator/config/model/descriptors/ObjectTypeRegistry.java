/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.AbstractConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * 
 */
public interface ObjectTypeRegistry {

    /**
     * @param objectType
     * @return the descriptor for this type
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> ObjectTypeDescriptor<T> get ( Class<T> objectType ) throws ModelServiceException;


    /**
     * @param type
     * @return the descriptor for this concrete type
     * @throws ModelServiceException
     */
    <T extends ConfigurationObject> ConcreteObjectTypeDescriptor<T, ? extends AbstractConfigurationObject<?>> getConcrete ( Class<? extends T> type )
            throws ModelServiceException;


    /**
     * 
     * @param objectType
     * @return the descriptor for this type
     * @throws ModelServiceException
     */
    ObjectTypeDescriptor<@Nullable ? extends ConfigurationObject> get ( String objectType ) throws ModelServiceException;


    /**
     * @param objectTypeName
     * @return the descriptor for this concrete type
     * @throws ModelServiceException
     */
    ConcreteObjectTypeDescriptor<@Nullable ?, @Nullable ? extends AbstractConfigurationObject<?>> getConcrete ( String objectTypeName )
            throws ModelServiceException;


    /**
     * @return the known object type names
     */
    Set<String> getObjectTypes ();


    /**
     * 
     * @param objectType
     * @return the types that inherit from the given type, i.e. can be used for type
     * @throws ModelServiceException
     */
    Set<ConcreteObjectTypeDescriptor<? extends ConfigurationObject, ? extends AbstractConfigurationObject<?>>> getApplicableTypes ( String objectType )
            throws ModelServiceException;


    /**
     * @param parent
     * @return the types that have the given parent type
     * @throws ModelServiceException
     */
    Set<ObjectTypeDescriptor<?>> getByParent ( Class<? extends ConfigurationObject> parent ) throws ModelServiceException;


    /**
     * 
     * @param parent
     * @return the concrete types that have the given parent type
     * @throws ModelServiceException
     */
    Set<ConcreteObjectTypeDescriptor<?, ?>> getConcreteByParent ( Class<? extends ConfigurationObject> parent ) throws ModelServiceException;

}
