/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationInstance;


/**
 * @author mbechler
 * 
 */
public interface ServiceTypeRegistry {

    /**
     * @param type
     * @return the descriptor for the given named type
     * @throws ModelServiceException
     */
    ServiceTypeDescriptor<@NonNull ConfigurationInstance, @NonNull ConfigurationInstance> getDescriptor ( String type ) throws ModelServiceException;


    /**
     * @param type
     * @return the descriptor for the given configuration instance type
     * @throws ModelServiceException
     */
    <T extends ConfigurationInstance> @NonNull ServiceTypeDescriptor<T, T> getDescriptor ( Class<T> type ) throws ModelServiceException;


    /**
     * @return the known service types
     */
    Set<String> getServiceTypes ();

}