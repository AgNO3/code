/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.service.impl;


import java.util.UUID;

import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;


/**
 * @author mbechler
 *
 */
public interface ConfigTestResultCache {

    /**
     * @param sequence
     * @param r
     */
    void update ( long sequence, ConfigTestResultImpl r );


    /**
     * @param testId
     * @return the stored test result, or null if not found
     */
    ConfigTestResultImpl get ( UUID testId );

}
