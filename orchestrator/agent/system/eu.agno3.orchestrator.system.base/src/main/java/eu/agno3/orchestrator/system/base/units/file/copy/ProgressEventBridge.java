/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.copy;


import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.progress.ProgressEventImpl;
import eu.agno3.orchestrator.system.file.util.ProgressCallback;


/**
 * @author mbechler
 * 
 */
public class ProgressEventBridge implements ProgressCallback {

    private Context ctx;
    private long total;


    /**
     * @param ctx
     */
    public ProgressEventBridge ( Context ctx ) {
        this.ctx = ctx;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.file.util.ProgressCallback#start(long)
     */
    @Override
    public void start ( long size ) {
        this.total = size;
        this.ctx.publishEvent(new ProgressEventImpl(this.ctx, 0.0f));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.file.util.ProgressCallback#progress(long)
     */
    @Override
    public void progress ( long pos ) {
        float percent = (float) ( (double) pos / (double) this.total );
        this.ctx.publishEvent(new ProgressEventImpl(this.ctx, percent));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.file.util.ProgressCallback#finished()
     */
    @Override
    public void finished () {
        this.ctx.publishEvent(new ProgressEventImpl(this.ctx, 1.0f));
    }

}
