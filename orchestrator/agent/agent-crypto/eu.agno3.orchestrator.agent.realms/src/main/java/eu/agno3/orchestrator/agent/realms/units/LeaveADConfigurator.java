/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 13, 2017 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.security.credentials.WrappedCredentials;


/**
 * @author mbechler
 *
 */
public class LeaveADConfigurator extends RealmConfigurator<StatusOnlyResult, LeaveAD, LeaveADConfigurator> {

    /**
     * @param unit
     */
    public LeaveADConfigurator ( LeaveAD unit ) {
        super(unit);
    }


    /**
     * @param username
     * @return this configurator
     */
    public LeaveADConfigurator user ( String username ) {
        getExecutionUnit().setUser(username);
        return self();
    }


    /**
     * @param password
     * @return this configurator
     */
    public LeaveADConfigurator password ( String password ) {
        getExecutionUnit().setPassword(password);
        return self();
    }


    /**
     * @param wrapped
     * @return this configurator
     */
    public LeaveADConfigurator creds ( WrappedCredentials wrapped ) {
        getExecutionUnit().setCredentials(wrapped);
        return self();
    }

}
