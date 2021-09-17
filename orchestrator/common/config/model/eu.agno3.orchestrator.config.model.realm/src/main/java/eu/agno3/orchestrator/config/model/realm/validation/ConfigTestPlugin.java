/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import java.util.Set;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ConfigTestPlugin <T extends ConfigurationObject> {

    /**
     * 
     * @return configuration type tested
     */
    Class<T> getTargetType ();


    /**
     * 
     * @return where to run this plugin
     */
    Set<ConfigTestPluginRunOn> getRunOn ();


    /**
     * 
     * @param config
     * @param r
     * @param params
     * @return a configuration test
     * @throws ModelServiceException
     */
    @SuppressWarnings ( "javadoc" )
    ConfigTestResult test ( T config, ConfigTestContext ctx, ConfigTestResult r, ConfigTestParams params ) throws ModelServiceException;
}
