/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


/**
 * @author mbechler
 *
 */
public interface LogTimerExpiredRunnable {

    /**
     * @param context
     */
    void expired ( ProcessorContext context );

}
