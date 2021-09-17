/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.validation.internal;


import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class ConfigTestContextImpl implements ConfigTestContext {

    private UserPrincipal owner;


    /**
     * @param userPrincipal
     */
    public ConfigTestContextImpl ( UserPrincipal userPrincipal ) {
        this.owner = userPrincipal;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext#getOwner()
     */
    @Override
    public UserPrincipal getOwner () {
        return this.owner;
    }

}
