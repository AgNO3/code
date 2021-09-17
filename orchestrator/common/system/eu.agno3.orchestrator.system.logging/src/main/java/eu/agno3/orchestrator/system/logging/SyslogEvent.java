/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2016 by mbechler
 */
package eu.agno3.orchestrator.system.logging;


import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;

import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventSeverity;


/**
 * @author mbechler
 *
 */
public class SyslogEvent implements Event, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -5122736320675425903L;

    private String id;
    private String dedupKey;
    private DateTime timestamp;
    private DateTime expiration;
    private EventSeverity severity;
    private String tag;

    private String objectId;
    private String agentId;

    private String message;
    private Map<String, Object> properties;

    private long fineTimestamp;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getId()
     */
    @Override
    public String getId () {
        return this.id;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getDedupKey()
     */
    @Override
    public String getDedupKey () {
        return this.dedupKey;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getTimestamp()
     */
    @Override
    public DateTime getTimestamp () {
        return this.timestamp;
    }


    /**
     * @return the fineTimestamp
     */
    public long getFineTimestamp () {
        return this.fineTimestamp;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getExpiration()
     */
    @Override
    public DateTime getExpiration () {
        return this.expiration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getSeverity()
     */
    @Override
    public EventSeverity getSeverity () {
        return this.severity;
    }


    /**
     * @return the message
     */
    public String getMessage () {
        return this.message;
    }


    /**
     * @return the tag
     */
    public String getTag () {
        return this.tag;
    }


    /**
     * @return the agentId
     */
    public String getAgentId () {
        return this.agentId;
    }


    /**
     * @return the objectId
     */
    public String getObjectId () {
        return this.objectId;
    }


    /**
     * @return the properties
     */
    public Map<String, Object> getProperties () {
        return this.properties;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.Event#getType()
     */
    @Override
    public String getType () {
        return "syslog-event"; //$NON-NLS-1$
    }


    /**
     * @param value
     * @return an event
     * @throws InvalidLogEntryException
     */
    public static Event fromMap ( Map<String, Object> value ) throws InvalidLogEntryException {
        SyslogEvent ev = new SyslogEvent();
        ev.id = (String) value.get(LogFields.CURSOR);
        if ( value.containsKey(LogFields.TIMESTAMP) ) {
            String tsField = (String) value.get(LogFields.TIMESTAMP);
            ev.fineTimestamp = Long.parseLong(tsField);
            ev.timestamp = new DateTime(ev.fineTimestamp / 1000);
        }
        ev.message = (String) value.get(LogFields.MESSAGE);
        if ( value.containsKey(LogFields.LEVEL) ) {
            ev.severity = levelToSeverity((String) value.get(LogFields.LEVEL));
        }
        else {
            ev.severity = EventSeverity.UNKNOWN;
        }

        Object objid = value.get(LogFields.OBJECT_ID);
        if ( objid instanceof UUID ) {
            ev.objectId = ( (UUID) objid ).toString();
        }
        else if ( objid instanceof String ) {
            ev.objectId = (String) objid;
        }

        Object agentid = value.get(LogFields.AGENT_ID);
        if ( agentid instanceof UUID ) {
            ev.agentId = ( (UUID) agentid ).toString();
        }
        else if ( agentid instanceof String ) {
            ev.agentId = (String) agentid;
        }

        ev.tag = (String) value.get(LogFields.TAG);

        checkEntry(ev);
        value.remove(LogFields.CURSOR);
        value.remove(LogFields.TIMESTAMP);
        value.remove(LogFields.MESSAGE);
        ev.properties = value;
        return ev;
    }


    /**
     * @param ev
     * @throws InvalidLogEntryException
     */
    private static void checkEntry ( SyslogEvent ev ) throws InvalidLogEntryException {
        if ( ev.id == null ) {
            throw new InvalidLogEntryException("Log entry missing ID"); //$NON-NLS-1$
        }
        else if ( ev.timestamp == null ) {
            throw new InvalidLogEntryException("Log entry missing Timestamp"); //$NON-NLS-1$
        }
        else if ( ev.message == null ) {
            throw new InvalidLogEntryException("Log entry missing Message"); //$NON-NLS-1$
        }
        else if ( ev.severity == null ) {
            throw new InvalidLogEntryException("Log entry missing Severity"); //$NON-NLS-1$
        }
    }


    /**
     * @param severity
     * @return
     */
    private static EventSeverity levelToSeverity ( String severity ) {
        int prio = Integer.parseInt(severity);

        if ( prio < 4 ) {
            return EventSeverity.ERROR;
        }

        switch ( prio ) {
        case 4:
            return EventSeverity.WARNING;
        case 5:
        case 6:
            return EventSeverity.INFO;
        case 7:
            return EventSeverity.TRACE;
        default:
            return EventSeverity.UNKNOWN;
        }
    }

}
