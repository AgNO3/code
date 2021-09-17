/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.05.2015 by mbechler
 */
package eu.agno3.runtime.eventlog;


import java.util.concurrent.Future;

import org.joda.time.DateTime;

import eu.agno3.runtime.eventlog.impl.MapEvent;


/**
 * @author mbechler
 *
 */
public class PropertiesEventBuilder implements EventBuilder<PropertiesEventBuilder> {

    private MapEvent event;
    private EventLogger logger;


    /**
     * @param logger
     * 
     */
    public PropertiesEventBuilder ( EventLogger logger ) {
        this.logger = logger;
        this.event = new MapEvent();
        this.event.put(MapEvent.TIMESTAMP, Long.valueOf(System.currentTimeMillis()));
        this.event.put(MapEvent.SEVERITY, EventSeverity.INFO.name());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventBuilder#severity(eu.agno3.runtime.eventlog.EventSeverity)
     */
    @Override
    public PropertiesEventBuilder severity ( EventSeverity severity ) {
        this.event.put(MapEvent.SEVERITY, severity.name());
        return this;
    }


    /**
     * @param dt
     * @return this builder
     */
    public PropertiesEventBuilder timestamp ( DateTime dt ) {
        this.event.put(MapEvent.TIMESTAMP, Long.valueOf(dt.getMillis()));
        return this;
    }


    /**
     * 
     * @param type
     * @return builder
     */
    public PropertiesEventBuilder type ( String type ) {
        this.event.put(MapEvent.TYPE, type);
        return this;
    }


    /**
     * 
     * @param key
     * @param value
     * @return builder
     */
    public PropertiesEventBuilder property ( String key, Object value ) {
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
