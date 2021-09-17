/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureKeytabExistsConfigurator extends RealmConfigurator<StatusOnlyResult, EnsureKeytabExists, EnsureKeytabExistsConfigurator> {

    /**
     * @param unit
     */
    protected EnsureKeytabExistsConfigurator ( EnsureKeytabExists unit ) {
        super(unit);
    }


    /**
     * 
     * @param keytab
     * @return this configurator
     */
    public EnsureKeytabExistsConfigurator keytab ( String keytab ) {
        getExecutionUnit().setKeytab(keytab);
        return this.self();
    }

}
