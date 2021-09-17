/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logging;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.runtime.eventlog.EventSeverity;


/**
 * @author mbechler
 *
 */
public class EventFilter {

    private DateTime startDate;
    private DateTime endDate;

    private EventSeverity filterSeverity;

    private StructuralObject anchor;

    private String filterTag;
    private String filterMessage;


    /**
     * @return the anchor
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    public void setAnchor ( StructuralObject anchor ) {
        this.anchor = anchor;
    }


    /**
     * @return the startDate
     */
    public DateTime getStartDate () {
        return this.startDate;
    }


    /**
     * @param startDate
     *            the startDate to set
     */
    public void setStartDate ( DateTime startDate ) {
        this.startDate = startDate;
    }


    /**
     * @return the endDate
     */
    public DateTime getEndDate () {
        return this.endDate;
    }


    /**
     * @param endDate
     *            the endDate to set
     */
    public void setEndDate ( DateTime endDate ) {
        this.endDate = endDate;
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
     * @return the filterTag
     */
    public String getFilterTag () {
        return this.filterTag;
    }


    /**
     * @param filterTag
     *            the filterTag to set
     */
    public void setFilterTag ( String filterTag ) {
        this.filterTag = filterTag;
    }

}
