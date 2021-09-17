/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;


/**
 * @author mbechler
 *
 */
public interface ConfigTestAsyncHandler {

    /**
     * 
     * @param tr
     * @return updated result
     */
    ConfigTestResultImpl update ( ConfigTestResult tr );
}
