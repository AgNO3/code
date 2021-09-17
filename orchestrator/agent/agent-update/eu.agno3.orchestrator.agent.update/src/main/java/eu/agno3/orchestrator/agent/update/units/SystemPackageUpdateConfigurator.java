/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import java.util.Collection;
import java.util.HashSet;

import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.update.SystemPackageTarget;


/**
 * @author mbechler
 *
 */
public class SystemPackageUpdateConfigurator
        extends AbstractConfigurator<SystemPackageUpdateResult, SystemPackageUpdate, SystemPackageUpdateConfigurator> {

    /**
     * @param unit
     */
    protected SystemPackageUpdateConfigurator ( SystemPackageUpdate unit ) {
        super(unit);
    }


    /**
     * @param systemPackageTarget
     * @return this
     */
    public SystemPackageUpdateConfigurator target ( SystemPackageTarget systemPackageTarget ) {
        this.getExecutionUnit().addTarget(systemPackageTarget);
        return this.self();
    }


    /**
     * @param repository
     * @return this
     */
    public SystemPackageUpdateConfigurator repository ( String repository ) {
        this.getExecutionUnit().setRepository(repository);
        return this.self();
    }


    /**
     * @param altRestartServices
     */
    public void altRestartServices ( Collection<String> altRestartServices ) {
        this.getExecutionUnit().setAlternativeRestartServices(new HashSet<>(altRestartServices));
    }

}
