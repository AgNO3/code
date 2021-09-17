/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.02.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.log.server.internal;


import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelObjectNotFoundException;
import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.InstanceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.server.service.InstanceServerService;
import eu.agno3.orchestrator.system.logging.InvalidLogEntryException;
import eu.agno3.orchestrator.system.logging.LogEvent;
import eu.agno3.orchestrator.system.logging.LogFields;
import eu.agno3.orchestrator.system.logging.SyslogEvent;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.messaging.listener.EventListener;
import eu.agno3.runtime.messaging.listener.RedeliveryPolicy;


/**
 * @author mbechler
 *
 */
@Component ( service = EventListener.class, property = "eventType=eu.agno3.orchestrator.system.logging.LogEvent" )
@RedeliveryPolicy ( maximumRedeliveries = 1, initialRedeliveryDelay = 100 )
public class LogEventListener implements EventListener<LogEvent> {

    private static final Logger log = Logger.getLogger(LogEventListener.class);

    private static final ObjectMapper OM = new ObjectMapper();
    private static final JsonFactory JF;

    private static final String SYSTEM = "system"; //$NON-NLS-1$

    private InstanceServerService instanceService;

    private Map<UUID, ObjectEntryCache> objectCache = Collections.synchronizedMap(new LRUMap<>(10));

    private int replayCacheSize = 10;
    private Queue<Map<String, Object>> replayCache = new CircularFifoQueue<>(this.replayCacheSize);


    static {
        OM.registerModule(new JodaModule());
        JF = new JsonFactory(OM);
    }

    private EventLogger evLog;


    @Reference
    protected synchronized void setEventLogger ( EventLogger el ) {
        this.evLog = el;
    }


    protected synchronized void unsetEventLogger ( EventLogger el ) {
        if ( this.evLog == el ) {
            this.evLog = null;
        }
    }


    @Reference
    protected synchronized void setInstanceService ( InstanceServerService iss ) {
        this.instanceService = iss;
    }


    protected synchronized void unsetInstanceService ( InstanceServerService iss ) {
        if ( this.instanceService == iss ) {
            this.instanceService = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull LogEvent event ) {
        int cnt = event.getCount();
        if ( log.isTraceEnabled() ) {
            log.trace("Have new events " + cnt); //$NON-NLS-1$
        }
        try ( JsonParser parser = JF.createParser(event.getBytes()) ) {
            List<Event> events = new ArrayList<>();
            for ( int i = 0; i < cnt; i++ ) {
                Map<String, Object> value = parser.readValueAs(Map.class);
                try {
                    if ( log.isTraceEnabled() ) {
                        log.trace("Event is " + value); //$NON-NLS-1$
                    }

                    // check and add instance/service id
                    @NonNull
                    UUID agentId = event.getOrigin().getAgentId();
                    value.put(LogFields.AGENT_ID, agentId);
                    if ( value.get(LogFields.OBJECT_ID) == null ) {
                        ObjectEntryCache instance = getInstanceId(agentId);
                        if ( instance != null ) {
                            value.put(LogFields.OBJECT_ID, instance.getObjectId());
                            value.put(LogFields.OBJECT_NAME, instance.getObjectName());
                        }
                    }

                    if ( this.replayCache.contains(value) ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug("Detected replayed message " + value); //$NON-NLS-1$
                        }
                        continue;
                    }

                    this.replayCache.add(value);
                    events.add(SyslogEvent.fromMap(value));

                }
                catch ( InvalidLogEntryException e ) {
                    log.warn("Invalid log entry", e); //$NON-NLS-1$
                }
            }

            if ( !events.isEmpty() ) {
                this.evLog.bulkLog(SYSTEM, events);
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to read log events", e); //$NON-NLS-1$
        }
    }


    /**
     * @param agentId
     * @return
     * @throws ModelObjectNotFoundException
     * @throws ModelServiceException
     */
    private ObjectEntryCache getInstanceId ( @NonNull UUID agentId ) {
        if ( this.objectCache.containsKey(agentId) ) {
            return this.objectCache.get(agentId);
        }

        InstanceStructuralObject instance = null;
        try {
            instance = this.instanceService.getInstanceForAgent(agentId);
        }
        catch ( ModelObjectNotFoundException e ) {
            log.debug("No instance for the agent " + agentId, e); //$NON-NLS-1$
        }
        catch (
            ModelServiceException |
            UndeclaredThrowableException e ) {
            log.warn("Failed to get instance for agent " + agentId, e); //$NON-NLS-1$
        }
        ObjectEntryCache ce = instance != null ? new ObjectEntryCache(instance.getId(), instance.getDisplayName()) : null;
        this.objectCache.put(agentId, ce);
        return ce;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<LogEvent> getEventType () {
        return LogEvent.class;
    }

    /**
     * @author mbechler
     *
     */
    public static class ObjectEntryCache {

        private UUID objectId;
        private String objectName;


        /**
         * @param objectId
         * @param objectname
         * 
         */
        public ObjectEntryCache ( UUID objectId, String objectname ) {
            this.objectId = objectId;
            this.objectName = objectname;
        }


        /**
         * @return the objectId
         */
        public UUID getObjectId () {
            return this.objectId;
        }


        /**
         * @return the objectName
         */
        public String getObjectName () {
            return this.objectName;
        }
    }
}
