/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ConfigTestPluginAsync <T extends ConfigurationObject> extends ConfigTestPlugin<T> {

    /**
     * 
     * @param config
     * @param ctx
     * @param params
     * @param r
     * @param h
     * @return a configuration test
     * @throws ModelServiceException
     */
    ConfigTestResult testAsync ( T config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r, ConfigTestAsyncHandler h )
            throws ModelServiceException;


    @Override
    default ConfigTestResult test ( T config, ConfigTestContext ctx, ConfigTestResult r, ConfigTestParams params ) throws ModelServiceException {
        return testAsync(config, ctx, params, r, new NullConfigTestAsyncHandler());
    }
}
