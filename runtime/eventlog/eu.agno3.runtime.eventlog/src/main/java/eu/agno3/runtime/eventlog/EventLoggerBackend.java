/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


/**
 * @author mbechler
 *
 */
public interface EventLoggerBackend {

    /**
     * @return the backend priority, higher comes first.
     */
    int getPriority ();


    /**
     * @param type
     * @param ev
     * @param eventTime
     * @param expireTime
     * @param bytes
     * @return a future for the completion of the logger operation
     */
    Future<?> log ( Event ev, byte[] bytes );


    /**
     * 
     */
    void runMaintenance ();


    /**
     * @param evs
     * @param data
     * @return a future for the completion of the logger operation
     */
    Future<?> bulkLog ( List<Event> evs, Map<Event, byte[]> data );


    /**
     * @return streams to send to this logger
     */
    Set<String> getIncludeStreams ();


    /**
     * 
     * @return streams to exclude from this logger
     */
    Set<String> getExcludeStreams ();


    /**
     * Only for testing, reinitialize logger
     */
    void reset ();

}
