/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.runtime.messaging.client.internal;


import javax.jms.ConnectionFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.client.MessagingClient;
import eu.agno3.runtime.messaging.client.MessagingClientFactory;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class DefaultMessagingClientRegistration {

    private static final Logger log = Logger.getLogger(DefaultMessagingClientRegistration.class);

    private MessagingClientFactory messageClientFactory;
    private ConnectionFactory connFactory;

    private MessageSource source;

    private MessagingClient<MessageSource> client;
    @SuppressWarnings ( "rawtypes" )
    private ServiceRegistration<MessagingClient> clientRegistration;


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {

        log.debug("Publishing default messaging client"); //$NON-NLS-1$
        try {
            MessageSource s = this.source;
            ConnectionFactory cf = this.connFactory;
            if ( s == null || cf == null ) {
                log.error("No message source or could not obtain connection"); //$NON-NLS-1$
                return;
            }
            MessagingClient<@NonNull MessageSource> cl = this.messageClientFactory.createTransactedClient(s, cf);
            cl.open();
            this.client = cl;
            this.clientRegistration = DsUtil.registerSafe(ctx, MessagingClient.class, cl, null);
        }
        catch ( MessagingException e ) {
            log.error("Failed to start message client:", e); //$NON-NLS-1$
            if ( this.client != null ) {
                try {
                    this.client.close();
                }
                catch ( MessagingException e1 ) {
                    log.debug("Failed to close client", e1); //$NON-NLS-1$
                }
            }
        }
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        log.debug("Unpublishing default messaging client"); //$NON-NLS-1$
        if ( this.clientRegistration != null ) {
            DsUtil.unregisterSafe(ctx, this.clientRegistration);
        }

        try {
            if ( this.client != null ) {
                this.client.close();
                this.client = null;
            }
        }
        catch ( MessagingException e ) {
            log.warn("Failed to close client:", e); //$NON-NLS-1$
        }
    }


    @Reference
    protected synchronized void setConnectionFactory ( ConnectionFactory cf ) {
        this.connFactory = cf;
    }


    protected synchronized void unsetConnectionFactory ( ConnectionFactory cf ) {
        if ( this.connFactory == cf ) {
            this.connFactory = null;
        }

    }


    @Reference
    protected synchronized void setMessagingClientFactory ( MessagingClientFactory mcf ) {
        this.messageClientFactory = mcf;
    }


    protected synchronized void unsetMessagingClientFactory ( MessagingClientFactory mcf ) {
        if ( this.messageClientFactory == mcf ) {
            this.messageClientFactory = null;
        }
    }


    @Reference
    protected synchronized void setMessageSource ( MessageSource s ) {
        this.source = s;
    }


    protected synchronized void unsetMessageSource ( MessageSource s ) {
        if ( this.source == s ) {
            this.source = null;
        }
    }

}
