/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import java.net.URI;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;
import eu.agno3.runtime.update.Feature;


/**
 * @author mbechler
 *
 */
public class P2UpdateConfigurator extends AbstractConfigurator<StatusOnlyResult, P2Update, P2UpdateConfigurator> {

    /**
     * @param unit
     */
    protected P2UpdateConfigurator ( P2Update unit ) {
        super(unit);
    }


    /**
     * 
     * @param service
     * @return this configurator
     */
    public P2UpdateConfigurator service ( ServiceStructuralObject service ) {
        this.getExecutionUnit().setTargetService(service);
        return this.self();
    }


    /**
     * @param restartPredicate
     * @return this configurator
     */
    public P2UpdateConfigurator forceOffline ( Predicate restartPredicate ) {
        this.getExecutionUnit().setForceOffline(restartPredicate);
        return this.self();
    }


    /**
     * @param repositories
     * @return this configurator
     */
    public P2UpdateConfigurator repositories ( Set<URI> repositories ) {
        this.getExecutionUnit().setRepositories(repositories);
        return this.self();
    }


    /**
     * @param target
     * @return this configurator
     */
    public P2UpdateConfigurator targets ( Set<Feature> target ) {
        this.getExecutionUnit().setTargets(target);
        return this.self();
    }


    /**
     * @param noApply
     * @return this configurator
     */
    public P2UpdateConfigurator noApply ( boolean noApply ) {
        this.getExecutionUnit().setNoApply(noApply);
        return this.self();
    }

}
