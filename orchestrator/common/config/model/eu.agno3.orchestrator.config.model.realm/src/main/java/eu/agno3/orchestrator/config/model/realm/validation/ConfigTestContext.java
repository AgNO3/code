/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation;


import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public interface ConfigTestContext {

    /**
     * 
     * @return the user requesting the test run
     */
    UserPrincipal getOwner ();
}
