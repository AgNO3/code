/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 *
 */
public class RemoveRealmConfigurator extends AbstractConfigurator<StatusOnlyResult, RemoveRealm, RemoveRealmConfigurator> {

    /**
     * @param unit
     */
    protected RemoveRealmConfigurator ( RemoveRealm unit ) {
        super(unit);
    }


    /**
     * 
     * @param realmName
     * @return this configurator
     */
    public RemoveRealmConfigurator realm ( String realmName ) {
        getExecutionUnit().setRealmName(realmName);
        return this.self();
    }


    /**
     * 
     * @param type
     * @return this configurator
     */
    public RemoveRealmConfigurator type ( RealmType type ) {
        getExecutionUnit().setRealmType(type);
        return this.self();
    }

}
