/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.List;
import java.util.concurrent.Future;


/**
 * @author mbechler
 *
 */
public interface EventLogger {

    /**
     * The default stream
     */
    public static final String DEFAULT_STREAM = "default"; //$NON-NLS-1$


    /**
     * Log a single event to the default stream
     * 
     * @param ev
     * @return a future to wait for the completion of the logger operation
     */
    public Future<Object> log ( Event ev );


    /**
     * Log a single event to a given stream
     * 
     * @param stream
     *            Stream to log to
     * @param ev
     * @return a future to wait for the completion of the logger operation
     */
    public Future<Object> log ( String stream, Event ev );


    /**
     * Log an event to the default stream
     * 
     * @param evs
     * @return a future to wait for the completion of the logger operation
     */
    public Future<Object> bulkLog ( List<Event> evs );


    /**
     * Log an event to a given stream
     * 
     * @param stream
     *            stream to log to
     * @param evs
     * @return a future to wait for the completion of the logger operation
     */
    public Future<Object> bulkLog ( String stream, List<Event> evs );


    /**
     * 
     * @param builder
     *            class
     * @return the event builder
     */
    public <T extends EventBuilder<T>> T build ( Class<T> builder );


    /**
     * 
     * @param builder
     * @return an audit context
     */
    public <T extends AuditEventBuilder<T>> AuditContext<T> audit ( Class<T> builder );


    /**
     * Testing only, reinitialize backends
     */
    void reset ();
}
