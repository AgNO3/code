/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2015 by mbechler
 */
package eu.agno3.runtime.eventlog.internal;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.eventlog.AuditContext;
import eu.agno3.runtime.eventlog.AuditEventBuilder;
import eu.agno3.runtime.eventlog.Event;
import eu.agno3.runtime.eventlog.EventBuilder;
import eu.agno3.runtime.eventlog.EventLogger;
import eu.agno3.runtime.eventlog.EventLoggerBackend;
import eu.agno3.runtime.eventlog.EventLoggerException;
import eu.agno3.runtime.eventlog.LogAnonymizer;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    EventLogger.class, EventLoggerImpl.class
} )
public class EventLoggerImpl implements EventLogger {

    private static final Logger log = Logger.getLogger(EventLoggerImpl.class);

    private SortedSet<EventLoggerBackend> backends = new TreeSet<>(new EventLoggerBackendComparator());

    private Map<String, Collection<EventLoggerBackend>> streamCache = new ConcurrentHashMap<>();

    private LogAnonymizer anonymizer;


    @Reference
    protected synchronized void setAnonymizer ( LogAnonymizer la ) {
        this.anonymizer = la;
    }


    protected synchronized void unsetAnonymizer ( LogAnonymizer la ) {
        if ( this.anonymizer == la ) {
            this.anonymizer = null;
        }
    }


    /**
     * 
     * @param elb
     */
    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    public synchronized void bindLoggerBackend ( EventLoggerBackend elb ) {
        this.backends.add(elb);
        this.streamCache.clear();
    }


    /**
     * 
     * @param elb
     */
    public synchronized void unbindLoggerBackend ( EventLoggerBackend elb ) {
        this.backends.remove(elb);
        this.streamCache.clear();
    }


    /**
     * @param stream
     * @return
     */
    protected Collection<? extends EventLoggerBackend> getBackends ( String s ) {
        String stream = s != null ? s : DEFAULT_STREAM;
        Collection<EventLoggerBackend> cached = this.streamCache.get(stream);
        if ( cached != null ) {
            return cached;
        }

        cached = new LinkedList<>();
        for ( EventLoggerBackend b : this.backends ) {
            if ( b.getIncludeStreams() != null && !b.getIncludeStreams().isEmpty() && !b.getIncludeStreams().contains(stream) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Skipping for include " + b.getIncludeStreams()); //$NON-NLS-1$
                }
                continue;
            }

            if ( b.getExcludeStreams() != null && !b.getExcludeStreams().isEmpty() && b.getExcludeStreams().contains(stream) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Skipping for exclude " + b.getExcludeStreams()); //$NON-NLS-1$
                }
                continue;
            }

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Adding logger %s for %s", b, stream)); //$NON-NLS-1$
            }
            cached.add(b);
        }
        this.streamCache.put(stream, cached);
        return cached;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#log(eu.agno3.runtime.eventlog.Event)
     */
    @Override
    public Future<Object> log ( Event ev ) {
        return log(null, ev);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#log(java.lang.String, eu.agno3.runtime.eventlog.Event)
     */
    @Override
    public CompositeFuture log ( String stream, Event ev ) {
        if ( ev.getSeverity() == null || StringUtils.isBlank(ev.getType()) || ev.getTimestamp() == null ) {
            throw new EventLoggerException("Illegal event"); //$NON-NLS-1$
        }

        byte[] data;
        try {
            data = convertEvent(stream, ev);
        }
        catch ( IOException e ) {
            throw new EventLoggerException("Failed to marshall event log entry", e); //$NON-NLS-1$
        }

        if ( log.isDebugEnabled() ) {
            String string = new String(data, StandardCharsets.UTF_8);
            log.debug( ( stream != null ? stream : DEFAULT_STREAM ) + ":" + string); //$NON-NLS-1$
        }

        List<Future<?>> futures = new LinkedList<>();
        for ( EventLoggerBackend backend : getBackends(stream) ) {
            try {
                futures.add(backend.log(ev, data));
            }
            catch ( Exception e ) {
                log.error("Logger backend returned synchronous error", e); //$NON-NLS-1$
            }
        }

        return new CompositeFuture(futures);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#bulkLog(java.util.List)
     */
    @Override
    public Future<Object> bulkLog ( List<Event> evs ) {
        return bulkLog(null, evs);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#bulkLog(java.lang.String, java.util.List)
     */
    @Override
    public CompositeFuture bulkLog ( String stream, List<Event> evs ) {
        Map<Event, byte[]> data = new LinkedHashMap<>();
        Collection<? extends EventLoggerBackend> useBackends = getBackends(stream);
        for ( Event ev : evs ) {
            if ( ev.getSeverity() == null || StringUtils.isBlank(ev.getType()) || ev.getTimestamp() == null ) {
                log.warn("Illegal event in bulk log"); //$NON-NLS-1$
                continue;
            }

            byte[] d;
            try {
                d = convertEvent(stream, ev);
            }
            catch ( IOException e ) {
                log.warn("Failed to marshal event in bulk log", e); //$NON-NLS-1$
                continue;
            }
            data.put(ev, d);
        }

        List<Future<?>> futures = new LinkedList<>();
        for ( EventLoggerBackend backend : useBackends ) {
            try {
                futures.add(backend.bulkLog(evs, data));
            }
            catch ( Exception e ) {
                log.error("Logger backend returned synchronous error", e); //$NON-NLS-1$
            }
        }

        return new CompositeFuture(futures);
    }


    /**
     * @param ev
     * @return
     * @throws IOException
     */
    @SuppressWarnings ( "unchecked" )
    byte[] convertEvent ( String stream, Event ev ) throws IOException {
        Map<?, ?> map = EventMarshaller.getObjectMapper().convertValue(ev, Map.class);
        if ( this.anonymizer != null ) {
            map = this.anonymizer.anonymize(stream, (Map<Object, Object>) map);
        }
        return EventMarshaller.marshallEvent(map);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#build(java.lang.Class)
     */
    @SuppressWarnings ( {
        "unchecked"
    } )
    @Override
    public <T extends EventBuilder<T>> T build ( Class<T> builder ) {

        try {
            Constructor<? extends EventBuilder<T>> constructor = builder.getConstructor(EventLogger.class);
            return (T) constructor.newInstance(this);
        }
        catch (
            NoSuchMethodException |
            SecurityException |
            InstantiationException |
            IllegalAccessException |
            IllegalArgumentException |
            InvocationTargetException e ) {
            throw new EventLoggerException("Failed to construct event builder", e); //$NON-NLS-1$
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.eventlog.EventLogger#audit(java.lang.Class)
     */
    @Override
    public <T extends AuditEventBuilder<T>> AuditContext<T> audit ( Class<T> builder ) {
        return new AuditContextImpl<>(this.build(builder));
    }


    /**
     * Runs the logger backend maintenance
     */
    public void runMaintenance () {
        for ( EventLoggerBackend backend : this.backends ) {
            try {
                backend.runMaintenance();
            }
            catch ( Exception e ) {
                log.error("Exception in logger backend maintenance:", e); //$NON-NLS-1$
            }
        }
    }


    @Override
    public void reset () {
        for ( EventLoggerBackend backend : this.backends ) {
            try {
                backend.reset();
            }
            catch ( Exception e ) {
                log.error("Exception in logger backend reset:", e); //$NON-NLS-1$
            }
        }
    }

}
