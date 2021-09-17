/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface LogProcessorPlugin {

    /**
     * 
     * @return processor priority, higher means called earlier
     */
    float getPriority ();


    /**
     * 
     * @param ev
     * @return whether this processor want's to process the event
     */
    boolean matches ( Map<String, Object> ev );


    /**
     * 
     * @param ctx
     * @param ev
     * @return the action to take for the message
     */
    LogAction process ( ProcessorContext ctx, Map<String, Object> ev );

}
