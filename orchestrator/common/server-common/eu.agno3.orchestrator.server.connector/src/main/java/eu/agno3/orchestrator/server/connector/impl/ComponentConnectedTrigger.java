/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2016 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.jms.XAConnectionFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.update.PlatformActivated;


/**
 * @author mbechler
 *
 */
public class ComponentConnectedTrigger implements Runnable {

    private static final Logger log = Logger.getLogger(ComponentConnectedTrigger.class);

    private XAConnectionFactory connFactory;
    private ServerConnector<?> connector;
    private PlatformActivated activated;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {

        @Override
        public Thread newThread ( Runnable r ) {
            return new Thread(r, "Component connected trigger"); //$NON-NLS-1$
        }
    });


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        // don't block DS and give the listener components some time to be activated
        this.executor.schedule(this, 2, TimeUnit.SECONDS);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        this.executor.shutdownNow();
        try {
            if ( !this.executor.awaitTermination(2, TimeUnit.SECONDS) ) {
                log.warn("Failed to stop connected trigger executor"); //$NON-NLS-1$
            }
        }
        catch ( InterruptedException e ) {
            log.warn("Interrupted waiting for connected trigger executor", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setXAConnection ( XAConnectionFactory c ) {
        this.connFactory = c;
    }


    protected synchronized void unsetXAConnection ( XAConnectionFactory c ) {
        if ( this.connFactory == c ) {
            this.connFactory = null;
        }
    }


    @Reference
    protected synchronized void setServerConnector ( ServerConnector<?> sc ) {
        this.connector = sc;
    }


    protected synchronized void unsetServerConnector ( ServerConnector<?> sc ) {
        if ( this.connector == sc ) {
            this.connector = null;
        }
    }


    @Reference
    protected synchronized void setActivated ( PlatformActivated pa ) {
        this.activated = pa;
    }


    protected synchronized void unsetActivated ( PlatformActivated pa ) {
        if ( this.activated == pa ) {
            this.activated = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        ServerConnector<?> conn = this.connector;
        if ( conn instanceof AbstractServerConnector ) {
            try {
                log.debug("Sending connected event"); //$NON-NLS-1$
                ( (AbstractServerConnector<@NonNull ?, ?>) conn ).connected();
            }
            catch (
                MessagingException |
                InterruptedException e ) {
                log.error("Failed to send connected event", e); //$NON-NLS-1$
            }
        }
        else {
            log.warn("Invalid connection"); //$NON-NLS-1$
        }
    }

}
