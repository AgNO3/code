/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.push;


import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.ops4j.pax.cdi.api.OsgiService;
import org.primefaces.push.EventBus;
import org.primefaces.push.RemoteEndpoint;
import org.primefaces.push.annotation.OnClose;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.OnOpen;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.annotation.Singleton;

import eu.agno3.orchestrator.server.gui.async.CometConnectionListener;
import eu.agno3.runtime.cdi.comet.CometPublisher;


/**
 * @author mbechler
 *
 */
@PushEndpoint ( "/primepush/events/{sessId}" )
@Singleton
public class EventsPushResource implements CometPublisher {

    private static final Logger log = Logger.getLogger(EventsPushResource.class);

    private EventBus bus;

    @Inject
    @OsgiService ( dynamic = true, timeout = 100 )
    private CometConnectionListener listener;


    /**
     * 
     */
    public EventsPushResource () {}


    /**
     * 
     * @param r
     * @param e
     */
    @OnOpen
    public void onOpen ( RemoteEndpoint r, EventBus e ) {
        ensureEventBus(e);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("COMET Connection to %s opened (remote=%s, transport=%s)", r.path(), r.address(), r.transport())); //$NON-NLS-1$
        }

        if ( this.listener == null ) {
            log.error("Connection listener not injected"); //$NON-NLS-1$
        }
        else {
            this.listener.onOpen(r, e);
        }
    }


    /**
     * 
     * @param message
     * @return the message
     */
    @OnMessage
    public String onMessage ( String message ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Publishing message " + message); //$NON-NLS-1$
        }
        return message;
    }


    /**
     * @param e
     */
    private synchronized void ensureEventBus ( EventBus e ) {
        if ( this.bus == null ) {
            this.bus = e;
        }
        else if ( this.bus != e ) {
            throw new IllegalArgumentException("Called multiple times with different event bussed"); //$NON-NLS-1$
        }
    }


    /**
     * @return the bus
     */
    public EventBus getBus () {
        if ( this.bus == null ) {
            throw new IllegalStateException("Bus is not initialized"); //$NON-NLS-1$
        }
        return this.bus;
    }


    /**
     * 
     * @param r
     * @param e
     */
    @OnClose
    public void onClose ( RemoteEndpoint r, EventBus e ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("COMET Connection to %s closed (remote=%s, transport=%s)", r.path(), r.address(), r.transport())); //$NON-NLS-1$
        }

        try {
            if ( this.listener == null ) {
                log.error("Connection listener not injected"); //$NON-NLS-1$
            }
            else {
                this.listener.onClose(r, e);
            }
        }
        catch ( IllegalStateException ex ) {
            log.debug("Failed to notify of closing", ex); //$NON-NLS-1$
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.cdi.comet.CometPublisher#publish(java.lang.String, java.lang.Object)
     */
    @Override
    public void publish ( String path, Object o ) {
        getBus().publish(path, o);
    }
}
