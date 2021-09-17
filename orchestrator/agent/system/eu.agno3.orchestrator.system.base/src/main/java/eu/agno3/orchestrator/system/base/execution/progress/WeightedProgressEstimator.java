/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import java.util.EnumMap;
import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.Job;
import eu.agno3.orchestrator.system.base.execution.Phase;


/**
 * Basic estimator
 * 
 * This estimate considers each unit as equally time consuming and
 * weights the phases with
 * VALIDATE 1
 * PREPARE 100
 * EXECUTE/ROLLBACK 100
 * CLEANUP 1
 * 
 * @author mbechler
 * 
 */
public class WeightedProgressEstimator implements ProgressEstimator {

    private static final int TOTAL_PHASES = Phase.values().length - 1;
    private int[] phaseWeights = new int[] {
        1, 200, 100, 1
    };


    protected int getTotalPhaseWeights () {
        return getTotalPhaseWeightsUpTo(TOTAL_PHASES);
    }


    protected int getPhaseWeight ( int pos ) {
        return this.phaseWeights[ pos ];
    }


    protected int getTotalPhaseWeightsUpTo ( int pos ) {
        int sum = 0;
        for ( int i = 0; i < Math.min(pos, this.phaseWeights.length); i++ ) {
            sum += this.getPhaseWeight(i);
        }
        return sum;
    }


    /**
     * @param totalUnits
     * @return
     */
    protected float getTotalUnitWeights ( Job job ) {
        return job.getExecutionUnits().size();
    }


    protected int getUnitWeight ( Job job, int pos ) {
        return 1;
    }


    protected int getTotalUnitWeightsUpTo ( Job job, int pos ) {
        int sum = 0;
        for ( int i = 0; i < Math.min(pos, job.getExecutionUnits().size()); i++ ) {
            sum += this.getUnitWeight(job, pos);
        }
        return sum;
    }


    @Override
    public float estimateOverallProgress ( Job job, Phase currentPhase, int unitPos, float unitProgress ) {
        int phasePos = phaseToPosition(currentPhase);
        float totalWidth = getTotalPhaseWeights();
        float phaseWidth = getPhaseWeight(phasePos);
        float phaseOffset = getTotalPhaseWeightsUpTo(phasePos);

        float scaledUnitWidth = phaseWidth / this.getTotalUnitWeights(job);
        float thisUnitWidth = this.getUnitWeight(job, unitPos) * scaledUnitWidth;
        float thisUnitOffset = this.getTotalUnitWeightsUpTo(job, unitPos) * scaledUnitWidth;
        float thisOffset = phaseOffset + thisUnitOffset;

        float scaledUnitProgress = unitProgress * thisUnitWidth;
        float at = ( thisOffset + scaledUnitProgress ) / totalWidth;
        return at;
    }

    private static final Map<Phase, Integer> PHASE_TO_POSITION = new EnumMap<>(Phase.class);

    static {
        PHASE_TO_POSITION.put(Phase.VALIDATE, 0);
        PHASE_TO_POSITION.put(Phase.PREPARE, 1);
        PHASE_TO_POSITION.put(Phase.EXECUTE, 2);
        PHASE_TO_POSITION.put(Phase.SUSPEND, 2);
        PHASE_TO_POSITION.put(Phase.RESUME, 2);
        PHASE_TO_POSITION.put(Phase.ROLLBACK, 2);
        PHASE_TO_POSITION.put(Phase.CLEANUP, 3);
    }


    private static final int phaseToPosition ( Phase p ) {
        Integer pos = PHASE_TO_POSITION.get(p);

        if ( pos == null ) {
            throw new IllegalArgumentException("Unimplemented phase " + p); //$NON-NLS-1$
        }

        return pos;
    }
}
