/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public class EventFilter implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 2630592690127823966L;

    private DateTime startTime;
    private DateTime endTime;

    private EventSeverity filterSeverity;

    private String filterMessage;

    private Map<String, String> filterProperties = new HashMap<>();


    /**
     * @return the startDate
     */
    public DateTime getStartTime () {
        return this.startTime;
    }


    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartTime ( DateTime startDate ) {
        this.startTime = startDate;
    }


    /**
     * @return the endDate
     */
    public DateTime getEndTime () {
        return this.endTime;
    }


    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndTime ( DateTime endDate ) {
        this.endTime = endDate;
    }


    /**
     * @return the filterMessage
     */
    public String getFilterMessage () {
        return this.filterMessage;
    }


    /**
     * @param filterMessage
     *            the filterMessage to set
     */
    public void setFilterMessage ( String filterMessage ) {
        this.filterMessage = filterMessage;
    }


    /**
     * @return the filterSeverity
     */
    public EventSeverity getFilterSeverity () {
        return this.filterSeverity;
    }


    /**
     * @param filterSeverity
     *            the filterSeverity to set
     */
    public void setFilterSeverity ( EventSeverity filterSeverity ) {
        this.filterSeverity = filterSeverity;
    }


    /**
     * @return the filterProperties
     */
    public Map<String, String> getFilterProperties () {
        return this.filterProperties;
    }


    /**
     * @param filterProperties
     *            the filterProperties to set
     */
    public void setFilterProperties ( Map<String, String> filterProperties ) {
        this.filterProperties = filterProperties;
    }

}
