/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.concurrent.Future;

import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public class PropertiesAuditEventBuilder implements AuditEventBuilder<PropertiesAuditEventBuilder> {

    private MapEvent event;
    private EventLogger logger;


    /**
     * @param logger
     * 
     */
    public PropertiesAuditEventBuilder ( EventLogger logger ) {
        this.logger = logger;
        this.event = new MapEvent();
        this.event.put(MapEvent.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        this.event.put(MapEvent.SEVERITY, EventSeverity.AUDIT.name());
        this.event.put(MapEvent.AUDIT_STATUS, AuditStatus.SUCCESS.name());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#severity(eu.agno3.runtime.eventlog.EventSeverity)
     */
    @Override
    public PropertiesAuditEventBuilder severity ( EventSeverity severity ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#fail(eu.agno3.runtime.eventlog.AuditStatus)
     */
    @Override
    public PropertiesAuditEventBuilder fail ( AuditStatus reason ) {
        this.event.put(MapEvent.AUDIT_STATUS, reason.name());
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.AuditEventBuilder#fail(eu.agno3.runtime.eventlog.AuditStatus)
     */
    @Override
    public PropertiesAuditEventBuilder action ( String action ) {
        this.event.put(MapEvent.AUDIT_ACTION, action);
        return this;
    }


    /**
     * 
     * @param type
     * @return builder
     */
    public PropertiesAuditEventBuilder type ( String type ) {
        this.event.put(MapEvent.TYPE, type);
        return this;
    }


    /**
     * 
     * @param key
     * @param value
     * @return builder
     */
    public PropertiesAuditEventBuilder property ( String key, Object value ) {
        this.event.put(key, value);
        return this;
    }


    /**
     * {@inheritDoc}
     * 
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#log()
     */
    @Override
    public Future<Object> log () {
        return this.logger.log(this.event);
    }

}
