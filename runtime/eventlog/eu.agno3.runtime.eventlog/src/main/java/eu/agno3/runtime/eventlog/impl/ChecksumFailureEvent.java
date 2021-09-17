/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.06.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.impl;


import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventSeverity;


/**
 * @author mbechler
 *
 */
@JsonInclude ( JsonInclude.Include.NON_NULL )
public class ChecksumFailureEvent implements Event {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getId()
     */
    @Override
    public String getId () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getDedupKey()
     */
    @Override
    public String getDedupKey () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getTimestamp()
     */
    @Override
    public DateTime getTimestamp () {
        return DateTime.now();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getExpiration()
     */
    @Override
    public DateTime getExpiration () {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getSeverity()
     */
    @Override
    public EventSeverity getSeverity () {
        return EventSeverity.ERROR;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return "log-checksum-failure"; //$NON-NLS-1$;
    }

}
