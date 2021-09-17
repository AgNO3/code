/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.nio.file.attribute.UserPrincipal;

import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureKeytabAccessConfigurator extends RealmConfigurator<StatusOnlyResult, EnsureKeytabAccess, EnsureKeytabAccessConfigurator> {

    /**
     * @param unit
     */
    protected EnsureKeytabAccessConfigurator ( EnsureKeytabAccess unit ) {
        super(unit);
    }


    /**
     * 
     * @param keytab
     * @return this configurator
     */
    public EnsureKeytabAccessConfigurator keytab ( String keytab ) {
        getExecutionUnit().setKeytab(keytab);
        return this.self();
    }


    /**
     * 
     * @param username
     * @return this configurator
     */
    public EnsureKeytabAccessConfigurator user ( String username ) {
        getExecutionUnit().setUser(username);
        return this.self();
    }


    /**
     * 
     * @param user
     * @return this configurator
     */
    public EnsureKeytabAccessConfigurator user ( UserPrincipal user ) {
        return user(user.getName());
    }

}
