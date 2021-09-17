/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.user;


import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class EnsureGroupExistsConfigurator extends AbstractConfigurator<StatusOnlyResult, EnsureGroupExists, EnsureGroupExistsConfigurator> {

    /**
     * @param unit
     */
    public EnsureGroupExistsConfigurator ( EnsureGroupExists unit ) {
        super(unit);
    }


    /**
     * 
     * @param name
     * @return this configurator
     */
    public EnsureGroupExistsConfigurator group ( String name ) {
        this.getExecutionUnit().setName(name);
        return this.self();
    }


    /**
     * 
     * @return this configurator
     */
    public EnsureGroupExistsConfigurator system () {
        this.getExecutionUnit().setSystem(true);
        return this.self();
    }


    /**
     * 
     * @param gid
     * @return this configurator
     */
    public EnsureGroupExistsConfigurator gid ( int gid ) {
        this.getExecutionUnit().setGid(gid);
        return this.self();
    }
}
