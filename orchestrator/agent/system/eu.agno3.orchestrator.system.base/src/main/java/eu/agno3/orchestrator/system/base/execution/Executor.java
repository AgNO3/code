/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution;


/**
 * @author mbechler
 * 
 */
public interface Executor {

    /**
     * Run a single phase on some units
     * 
     * @param context
     *            context to run in
     * @param phase
     *            phase to run
     * @param toRun
     *            specify which execution units to run
     * @param keepGoing
     *            whether to continue executing after a failure
     * @return whether execution was suspended
     */
    boolean runPhase ( Context context, Phase phase, JobIterator toRun, boolean keepGoing );


    /**
     * @param context
     * @param phase
     * @param toRun
     * @param resume
     * @param keepGoing
     * @return whether execution was suspended
     */
    boolean runPhase ( Context context, Phase phase, JobIterator toRun, SuspendData resume, boolean keepGoing );


    /**
     * Run all phases on the given units
     * 
     * @param context
     *            run in context
     * @param job
     *            units to run
     * @return whether execution was suspended
     */
    boolean run ( Context context, Job job );


    /**
     * @param context
     * @param toRun
     * @param resume
     * @return whether execution was suspended
     */
    boolean run ( Context context, Job toRun, SuspendData resume );

}
