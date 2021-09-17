/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.system.base.execution.Runner;
import eu.agno3.orchestrator.system.base.execution.RunnerFactory;
import eu.agno3.orchestrator.system.base.execution.progress.ProgressTracker;


/**
 * @author mbechler
 * 
 */
@Component ( service = RunnerFactory.class )
public class RunnerFactoryImpl implements RunnerFactory {

    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.RunnerFactory#createRunner()
     */
    @Override
    public Runner createRunner () {
        RunnerImpl r = new RunnerImpl();
        ProgressTracker t = new ProgressTracker();
        r.registerEventListener(t);
        return r;
    }
}
