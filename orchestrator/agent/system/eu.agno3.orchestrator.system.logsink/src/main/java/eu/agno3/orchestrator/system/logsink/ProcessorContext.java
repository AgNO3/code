/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


import java.util.Map;
import java.util.concurrent.ScheduledFuture;


/**
 * @author mbechler
 *
 */
public interface ProcessorContext {

    /**
     * 
     * @param ev
     */
    void inject ( Map<String, Object> ev );


    /**
     * @param delay
     * @param t
     * @return future for the timer task
     */
    ScheduledFuture<?> addTimer ( int delay, LogTimerExpiredRunnable t );
}
