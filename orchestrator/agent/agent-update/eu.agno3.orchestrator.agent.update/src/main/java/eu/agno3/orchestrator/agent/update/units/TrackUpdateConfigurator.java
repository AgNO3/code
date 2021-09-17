/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.12.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.jobs.agent.backup.units.BackupResult;
import eu.agno3.orchestrator.system.base.execution.Predicate;
import eu.agno3.orchestrator.system.base.execution.ResultReference;
import eu.agno3.orchestrator.system.base.execution.impl.AbstractConfigurator;
import eu.agno3.orchestrator.system.base.execution.result.StatusOnlyResult;


/**
 * @author mbechler
 *
 */
public class TrackUpdateConfigurator extends AbstractConfigurator<StatusOnlyResult, TrackUpdate, TrackUpdateConfigurator> {

    /**
     * @param unit
     */
    protected TrackUpdateConfigurator ( TrackUpdate unit ) {
        super(unit);
        unit.setUpdateTimestamp(DateTime.now());
    }


    /**
     * @param sequence
     * @return this
     */
    public TrackUpdateConfigurator sequence ( long sequence ) {
        this.getExecutionUnit().setUpdatedSequence(sequence);
        return this.self();
    }


    /**
     * @param stream
     * @return this
     */
    public TrackUpdateConfigurator stream ( String stream ) {
        this.getExecutionUnit().setUpdatedStream(stream);
        return this.self();
    }


    /**
     * @param predicate
     * @return this
     */
    public TrackUpdateConfigurator suggestReboot ( Predicate predicate ) {
        this.getExecutionUnit().setSuggestRebootPredicate(predicate);
        return this.self();
    }


    /**
     * @param br
     * @return this
     */
    public TrackUpdateConfigurator backupRef ( ResultReference<BackupResult> br ) {
        this.getExecutionUnit().setBackupReference(br);
        return this.self();
    }
}
