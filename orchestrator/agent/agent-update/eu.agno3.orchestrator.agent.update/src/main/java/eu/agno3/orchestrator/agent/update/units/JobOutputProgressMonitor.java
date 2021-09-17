/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.units;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.progress.ProgressEventImpl;


/**
 * @author mbechler
 *
 */
public class JobOutputProgressMonitor implements IProgressMonitor {

    private int at;
    private int totalWork;

    private float percent;

    private @NonNull Context ctx;


    /**
     * @param ctx
     * 
     */
    public JobOutputProgressMonitor ( @NonNull Context ctx ) {
        this.ctx = ctx;
    }


    @Override
    public void worked ( int work ) {
        this.at += work;

    }


    @Override
    public void subTask ( String name ) {}


    @Override
    public void setTaskName ( String name ) {}


    @Override
    public void setCanceled ( boolean value ) {}


    @Override
    public boolean isCanceled () {
        return false;
    }


    @Override
    public void internalWorked ( double work ) {
        this.at += work;
        float lastPerc = this.percent;
        float newPerc = 100.0f * ( (float) this.at / (float) this.totalWork );

        if ( newPerc - lastPerc > 1.0 ) {
            this.percent = newPerc;
            this.ctx.publishEvent(new ProgressEventImpl(this.ctx, this.percent));
        }
    }


    @Override
    public void done () {
        this.at = 0;
    }


    @Override
    public void beginTask ( String name, int tw ) {
        this.ctx.getOutput().info("Start " + name); //$NON-NLS-1$
        this.totalWork = tw;

    }

}
