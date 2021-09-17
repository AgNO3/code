/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation.internal;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRegistry;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPluginRegistry.class )
public class ConfigTestPluginRegistryImpl implements ConfigTestPluginRegistry {

    private static final Logger log = Logger.getLogger(ConfigTestPluginRegistryImpl.class);

    private Map<Class<? extends ConfigurationObject>, ConfigTestPlugin<? extends ConfigurationObject>> plugins = new ConcurrentHashMap<>();

    private ObjectTypeRegistry typeRegistry;


    @Reference ( policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindPlugin ( ConfigTestPlugin<?> pl ) {
        if ( this.plugins.put(pl.getTargetType(), pl) != null ) {
            log.warn("Duplicate config test plugin for " + pl.getTargetType().getName()); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindPlugin ( ConfigTestPlugin<?> pl ) {
        this.plugins.remove(pl.getTargetType(), pl);
    }


    @Reference
    protected synchronized void setTypeRegistry ( ObjectTypeRegistry otr ) {
        this.typeRegistry = otr;
    }


    protected synchronized void unsetTypeRegistry ( ObjectTypeRegistry otr ) {
        if ( this.typeRegistry == otr ) {
            this.typeRegistry = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRegistry#getTestPlugin(java.lang.String)
     */
    @Override
    public ConfigTestPlugin<?> getTestPlugin ( String typeName ) {
        try {
            return getTestPlugin(this.typeRegistry.getConcrete(typeName).getObjectType());
        }
        catch ( ModelServiceException e ) {
            log.error("Failed to get test plugin for " + typeName); //$NON-NLS-1$
            return null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRegistry#getTestPlugin(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T extends ConfigurationObject> ConfigTestPlugin<T> getTestPlugin ( Class<T> type ) {
        return (ConfigTestPlugin<T>) this.plugins.get(type);
    }

}
