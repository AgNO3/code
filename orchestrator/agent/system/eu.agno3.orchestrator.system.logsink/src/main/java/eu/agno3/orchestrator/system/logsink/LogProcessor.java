/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logsink;


import java.util.Map;


/**
 * @author mbechler
 *
 */
public interface LogProcessor {

    /**
     * @param logEntry
     * @param sink
     * @return whether to suspend further delivery attempts
     */
    boolean process ( Map<String, Object> logEntry, InhibitableLogSource sink );

}
