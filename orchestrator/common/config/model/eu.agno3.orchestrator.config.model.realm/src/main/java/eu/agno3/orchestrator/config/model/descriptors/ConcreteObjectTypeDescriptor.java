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


/**
 * @author mbechler
 * @param <T>
 *            object type
 * @param <TImpl>
 * 
 */
public interface ConcreteObjectTypeDescriptor <T extends ConfigurationObject, TImpl extends AbstractConfigurationObject<T>>
        extends ObjectTypeDescriptor<T> {

    /**
     * @return the implementation type
     */
    @NonNull
    Class<TImpl> getImplementationType ();


    /**
     * 
     * @return A fresh instance of the type
     */
    @NonNull
    T newInstance ();


    /**
     * @return a global defaults instance for this type
     */
    @NonNull
    T getGlobalDefaults ();


    /**
     * @param type
     * @return whether this type descriptor overrides the defaults for the given type when embedded
     */
    boolean hasOverrideDefaultsFor ( @NonNull Class<? extends @Nullable ConfigurationObject> type );


    /**
     * @param type
     * @return the global defaults to be applied
     */
    <@Nullable TOverride extends ConfigurationObject> @Nullable TOverride getOverrideDefaults ( @NonNull Class<? extends TOverride> type );

}
