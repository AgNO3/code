/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.04.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.realms.units;


import eu.agno3.orchestrator.system.base.execution.Result;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.runtime.net.krb5.RealmType;


/**
 * @author mbechler
 * @param <TResult>
 * @param <TExecutionUnit>
 * @param <TConfigurator>
 *
 */
public abstract class RealmConfigurator <TResult extends Result, TExecutionUnit extends RealmExecutionUnit<TResult, TExecutionUnit, TConfigurator>, TConfigurator extends RealmConfigurator<TResult, TExecutionUnit, TConfigurator>>
        extends AbstractConfigurator<TResult, TExecutionUnit, TConfigurator> {

    /**
     * @param unit
     */
    public RealmConfigurator ( TExecutionUnit unit ) {
        super(unit);
    }


    /**
     * 
     * @param realmName
     * @return this configurator
     */
    public TConfigurator realm ( String realmName ) {
        getExecutionUnit().setRealmName(realmName);
        return this.self();
    }


    /**
     * 
     * @param type
     * @return this configurator
     */
    public TConfigurator type ( RealmType type ) {
        getExecutionUnit().setRealmType(type);
        return this.self();
    }

}