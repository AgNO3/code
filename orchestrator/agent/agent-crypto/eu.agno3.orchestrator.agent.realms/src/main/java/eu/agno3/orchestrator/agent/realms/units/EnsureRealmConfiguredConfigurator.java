/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import java.util.Arrays;
import java.util.Properties;

import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureRealmConfiguredConfigurator extends RealmConfigurator<StatusOnlyResult, EnsureConfiguredRealm, EnsureRealmConfiguredConfigurator> {

    /**
     * @param unit
     */
    protected EnsureRealmConfiguredConfigurator ( EnsureConfiguredRealm unit ) {
        super(unit);
    }


    /**
     * 
     * @param props
     * @return this configurator
     */
    public EnsureRealmConfiguredConfigurator config ( Properties props ) {
        getExecutionUnit().setProperties(props);
        return this.self();
    }


    /**
     * 
     * @param users
     *            user to allow access to this realm by default
     * @return this configurator
     */
    public EnsureRealmConfiguredConfigurator allowUsers ( String... users ) {
        getExecutionUnit().setDefaultAllowedUsers(Arrays.asList(users));
        return this.self();
    }

}
