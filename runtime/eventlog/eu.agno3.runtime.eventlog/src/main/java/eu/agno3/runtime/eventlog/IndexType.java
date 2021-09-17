/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


/**
 * @author mbechler
 *
 */
public enum IndexType {

    /**
     * An seperate index is used for every day
     */
    DAILY,

    /**
     * An seperate index is used for every week
     */
    WEEKLY,

    /**
     * An seperate index is used for every month
     */
    MONTHLY,

    /**
     * An seperate index is used for every year
     */
    YEARLY
}
