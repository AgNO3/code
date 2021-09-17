/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


/**
 * @author mbechler
 *
 */
public interface EventLogConfiguration {

    /**
     * 
     * @return the number of days the log data is kept
     */
    int getRetainDays ();


    /**
     * 
     * @return the number of days the log data is kept searchable
     */
    int getRetainIndexedDays ();


    /**
     * 
     * @return the number of days the log data is kept actively searchable
     */
    int getRetainOpenIndexedDays ();


    /**
     * 
     * @return the log base name
     */
    String getLogName ();


    /**
     * @return whether to discard events that are older than the retention time
     */
    boolean isIgnorePostdated ();


    /**
     * @return the indexing type (time range)
     */
    IndexType getIndexType ();

}
