/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 *
 */
public interface ConfigTestPluginRegistry {

    /**
     * 
     * @param typeName
     * @return config test plugin
     */
    ConfigTestPlugin<?> getTestPlugin ( String typeName );


    /**
     * 
     * @param type
     * @return config test plugin
     */
    <T extends ConfigurationObject> ConfigTestPlugin<T> getTestPlugin ( Class<T> type );
}
