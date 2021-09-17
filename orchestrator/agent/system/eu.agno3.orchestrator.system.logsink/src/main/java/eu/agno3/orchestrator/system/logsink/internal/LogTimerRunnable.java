/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink.internal;


import eu.agno3.orchestrator.system.logsink.LogTimerExpiredRunnable;
import eu.agno3.orchestrator.system.logsink.ProcessorContext;


/**
 * @author mbechler
 *
 */
public class LogTimerRunnable implements Runnable {

    private ProcessorContext context;
    private LogTimerExpiredRunnable runnable;


    /**
     * @param context
     * @param t
     */
    public LogTimerRunnable ( ProcessorContext context, LogTimerExpiredRunnable t ) {
        this.context = context;
        this.runnable = t;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        this.runnable.expired(this.context);
    }

}
