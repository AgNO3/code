/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.gui.async.internal;


import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;
import org.primefaces.push.EventBus;
import org.primefaces.push.RemoteEndpoint;

import eu.agno3.orchestrator.gui.connector.GuiEventListener;
import eu.agno3.orchestrator.gui.connector.GuiNotificationEvent;
import eu.agno3.orchestrator.server.gui.async.CometConnectionListener;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    GuiEventListener.class, CometConnectionListener.class
}, immediate = true, property = "eventType=eu.agno3.orchestrator.gui.connector.GuiNotificationEvent" )
public class GuiPushBridge implements GuiEventListener, CometConnectionListener {

    private static final Logger log = Logger.getLogger(GuiPushBridge.class);
    private static final String EVENTS_PREFIX = "/primepush/events/"; //$NON-NLS-1$
    private Map<String, Subject> knownSubjects = new ConcurrentHashMap<>();
    private Map<String, AtomicInteger> refcounts = new ConcurrentHashMap<>();

    private ScheduledExecutorService closeScheduler = Executors.newSingleThreadScheduledExecutor();
    private long closeDelay = 1000;
    private EventBus bus;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.gui.async.CometConnectionListener#onOpen(org.primefaces.push.RemoteEndpoint,
     *      org.primefaces.push.EventBus)
     */
    @Override
    public void onOpen ( RemoteEndpoint r, EventBus e ) {
        String sessId = fromPath(r.path());
        if ( log.isTraceEnabled() ) {
            log.trace("Adding subject " + SecurityUtils.getSubject()); //$NON-NLS-1$
        }
        incrementRefcount(sessId);
        this.knownSubjects.put(sessId, SecurityUtils.getSubject());
        if ( this.bus != e ) {
            this.bus = e;
        }
    }


    /**
     * @param sessId
     * @return
     */
    private int incrementRefcount ( String sessId ) {
        synchronized ( this.refcounts ) {
            AtomicInteger refcnt = this.refcounts.get(sessId);

            if ( refcnt == null ) {
                refcnt = new AtomicInteger(1);
                this.refcounts.put(sessId, refcnt);
                return 1;
            }
            return refcnt.incrementAndGet();
        }
    }


    private int decrementRefcount ( String sessId ) {
        synchronized ( this.refcounts ) {
            AtomicInteger refcnt = this.refcounts.get(sessId);
            if ( refcnt == null ) {
                return 0;
            }

            int cnt = refcnt.decrementAndGet();
            if ( cnt == 0 ) {
                this.refcounts.remove(refcnt);
                return 0;
            }

            return cnt;
        }
    }


    private int getRefcount ( String sessId ) {
        return this.refcounts.getOrDefault(sessId, new AtomicInteger()).get();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.gui.async.CometConnectionListener#onClose(org.primefaces.push.RemoteEndpoint,
     *      org.primefaces.push.EventBus)
     */
    @Override
    public void onClose ( RemoteEndpoint r, EventBus e ) {
        String sessId = fromPath(r.path());
        decrementRefcount(sessId);
        this.closeScheduler.schedule(new Runnable() {

            @Override
            public void run () {
                doRemove(sessId);
            }
        }, this.closeDelay, TimeUnit.MILLISECONDS);
    }


    void doRemove ( String sessId ) {
        int refcnt = getRefcount(sessId);
        if ( refcnt == 0 ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Removing subject " + SecurityUtils.getSubject()); //$NON-NLS-1$
            }
            this.knownSubjects.remove(sessId);
        }
        else {
            if ( log.isTraceEnabled() ) {
                log.trace(String.format("Not removing subject with refcnt %d %s", refcnt, SecurityUtils.getSubject())); //$NON-NLS-1$
            }
        }
    }


    /**
     * @param path
     * @return
     */
    private static String fromPath ( String path ) {
        return path.substring(EVENTS_PREFIX.length());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#onEvent(eu.agno3.runtime.messaging.msg.EventMessage)
     */
    @Override
    public void onEvent ( @NonNull GuiNotificationEvent event ) {
        String path = event.getPath();
        String payload = event.getPayload();
        if ( StringUtils.isBlank(path) ) {
            log.warn("Notification path is empty"); //$NON-NLS-1$
            return;
        }

        if ( this.bus == null ) {
            log.trace("No EventBus configured, no clients yet"); //$NON-NLS-1$
            return;
        }

        try {
            JSONObject evObj = new JSONObject();
            evObj.put("path", path); //$NON-NLS-1$
            if ( !StringUtils.isEmpty(payload) ) {
                evObj.put("payload", payload); //$NON-NLS-1$
            }

            Set<Entry<String, Subject>> sessions = this.knownSubjects.entrySet();
            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Publishing to %d active session: %s", sessions.size(), evObj.toString())); //$NON-NLS-1$
            }

            for ( Entry<String, Subject> sess : sessions ) {
                String target = EVENTS_PREFIX + sess.getKey();
                if ( log.isTraceEnabled() ) {
                    log.trace(String.format("Publishing to %s: %s", target, evObj.toString())); //$NON-NLS-1$
                }
                this.bus.publish(target, evObj.toString());
            }
        }
        catch ( JSONException e ) {
            log.warn("Failed to build JSON", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.EventListener#getEventType()
     */
    @Override
    public @NonNull Class<GuiNotificationEvent> getEventType () {
        return GuiNotificationEvent.class;
    }

}
