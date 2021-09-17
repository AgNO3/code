/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface Event {

    /**
     * 
     * @return an identifier if known
     */
    String getId ();


    /**
     * 
     * @return an deduplication key
     */
    String getDedupKey ();


    /**
     * 
     * @return the event timestamp
     */
    DateTime getTimestamp ();


    /**
     * @return the time the event should be removed automatically, null if not expiring
     */
    DateTime getExpiration ();


    /**
     * 
     * @return the event severity
     */
    EventSeverity getSeverity ();


    /**
     * @return the type identification
     */
    String getType ();

}
