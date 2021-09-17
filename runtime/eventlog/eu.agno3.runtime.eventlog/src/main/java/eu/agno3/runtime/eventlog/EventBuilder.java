/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.concurrent.Future;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface EventBuilder <T extends EventBuilder<T>> {

    /**
     * 
     * @param severity
     * @return this builder
     */
    T severity ( EventSeverity severity );


    /**
     * 
     * @return a future to wait for the completion of the logging operation
     * @throws EventLoggerException
     */
    Future<Object> log ();
}
