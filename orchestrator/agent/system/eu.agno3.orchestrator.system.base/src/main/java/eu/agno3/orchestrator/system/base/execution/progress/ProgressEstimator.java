/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.Phase;


/**
 * @author mbechler
 * 
 */
public interface ProgressEstimator {

    /**
     * @param job
     * @param currentPhase
     * @param unitPos
     * @param unitProgress
     * @return overall progress in the range [0.0,1.0]
     */
    float estimateOverallProgress ( Job job, Phase currentPhase, int unitPos, float unitProgress );

}
