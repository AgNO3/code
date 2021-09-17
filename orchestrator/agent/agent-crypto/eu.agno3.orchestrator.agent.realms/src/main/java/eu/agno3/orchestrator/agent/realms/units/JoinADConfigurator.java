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
public class JoinADConfigurator extends RealmConfigurator<StatusOnlyResult, JoinAD, JoinADConfigurator> {

    /**
     * @param unit
     */
    public JoinADConfigurator ( JoinAD unit ) {
        super(unit);
    }


    /**
     * @param username
     * @return this configurator
     */
    public JoinADConfigurator user ( String username ) {
        getExecutionUnit().setUser(username);
        return self();
    }


    /**
     * @param password
     * @return this configurator
     */
    public JoinADConfigurator password ( String password ) {
        getExecutionUnit().setPassword(password);
        return self();
    }


    /**
     * @param machinePassword
     * @return this configurator
     */
    public JoinADConfigurator usingMachinePassword ( String machinePassword ) {
        getExecutionUnit().setJoinUsingMachinePassword(true);
        getExecutionUnit().setMachinePassword(machinePassword);
        return self();
    }


    /**
     * @param wrapped
     * @return this configurator
     */
    public JoinADConfigurator creds ( WrappedCredentials wrapped ) {
        getExecutionUnit().setCredentials(wrapped);
        return self();
    }
}
