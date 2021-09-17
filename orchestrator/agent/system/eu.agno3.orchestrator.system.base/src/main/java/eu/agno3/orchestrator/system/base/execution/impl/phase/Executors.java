/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.impl.phase;


import java.util.EnumMap;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.Phase;
import eu.agno3.orchestrator.system.base.execution.PhaseExecutor;


/**
 * @author mbechler
 * 
 */
public final class Executors {

    private static Map<Phase, PhaseExecutor> PHASE_EXECUTORS = new EnumMap<>(Phase.class);
    static {
        PHASE_EXECUTORS.put(Phase.VALIDATE, new ValidatePhaseExecutor());
        PHASE_EXECUTORS.put(Phase.PREPARE, new PreparePhaseExecutor());
        PHASE_EXECUTORS.put(Phase.EXECUTE, new ExecutePhaseExecutor());
        PHASE_EXECUTORS.put(Phase.ROLLBACK, new RollbackPhaseExecutor());
        PHASE_EXECUTORS.put(Phase.CLEANUP, new CleanUpPhaseExecutor());
        PHASE_EXECUTORS.put(Phase.SUSPEND, new SuspendPhaseExecutor());
        PHASE_EXECUTORS.put(Phase.RESUME, new ResumePhaseExecutor());
    }


    private Executors () {}


    /**
     * @return all available phase executors
     */
    public static Map<Phase, PhaseExecutor> getPhaseExecutors () {
        return PHASE_EXECUTORS;
    }
}
