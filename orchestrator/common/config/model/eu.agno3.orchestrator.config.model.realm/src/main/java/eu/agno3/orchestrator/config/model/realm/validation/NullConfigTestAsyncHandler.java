/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;


/**
 * @author mbechler
 *
 */
public class NullConfigTestAsyncHandler implements ConfigTestAsyncHandler {

    @Override
    public ConfigTestResultImpl update ( ConfigTestResult tr ) {
        return tr.get();
    }
}
