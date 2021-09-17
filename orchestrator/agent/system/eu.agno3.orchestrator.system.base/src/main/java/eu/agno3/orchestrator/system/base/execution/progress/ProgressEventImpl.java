/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.progress;


import java.util.Map;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.events.AbstractExecutorEvent;


/**
 * @author mbechler
 * 
 */
public class ProgressEventImpl extends AbstractExecutorEvent implements ProgressEvent {

    private float progress;
    private Map<String, String> stateContext;
    private String stateMessage;


    /**
     * @param ctx
     * @param progress
     */
    public ProgressEventImpl ( Context ctx, float progress ) {
        super(ctx);
        this.progress = progress;
        this.stateMessage = ctx.getStateMessage();
        this.stateContext = ctx.getStateContext();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.progress.ProgressEvent#getProgress()
     */
    @Override
    public float getProgress () {
        return this.progress;
    }


    /**
     * @return the stateContext
     */
    @Override
    public Map<String, String> getStateContext () {
        return this.stateContext;
    }


    /**
     * @return the stateMessage
     */
    @Override
    public String getStateMessage () {
        return this.stateMessage;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("Progress %.3f", this.progress); //$NON-NLS-1$
    }
}
