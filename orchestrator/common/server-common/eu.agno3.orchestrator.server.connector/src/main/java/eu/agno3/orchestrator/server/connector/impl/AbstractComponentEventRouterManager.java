/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.09.2013 by mbechler
 */
package eu.agno3.orchestrator.server.connector.impl;


import java.util.Optional;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.routing.EventRouter;
import eu.agno3.runtime.messaging.routing.EventRouterManager;


/**
 * @author mbechler
 * @param <TConfig>
 * 
 */
public abstract class AbstractComponentEventRouterManager <@NonNull TConfig extends ComponentConfig> implements EventRouterManager {

    private static final Logger log = Logger.getLogger(AbstractComponentEventRouterManager.class);

    private Optional<TConfig> config = Optional.empty();

    private ComponentEventRouter cached;
    private Optional<TConfig> cachedForConfig = null;


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setComponentConfig ( TConfig c ) {
        this.config = Optional.ofNullable(c);
    }


    protected synchronized void unsetComponentConfig ( TConfig c ) {
        if ( this.config.equals(c) ) {
            this.config = Optional.empty();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.routing.EventRouterManager#getRouterFor(eu.agno3.runtime.messaging.addressing.EventScope)
     */
    @Override
    public EventRouter getRouterFor ( EventScope scope ) throws MessagingException {
        if ( !this.config.isPresent() ) {
            throw new MessagingException("Trying to use component event router while server connection is not available"); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Using component event router for " + scope); //$NON-NLS-1$
        }

        @NonNull
        TConfig compConfig = this.config.get();
        if ( this.cachedForConfig != this.config ) {
            try {
                if ( this.cached != null ) {
                    this.cached.close();
                }
            }
            catch ( JMSException e ) {
                log.warn("Failed to close producer", e); //$NON-NLS-1$
            }
            this.cached = null;
            this.cachedForConfig = this.config;
        }
        if ( this.cached == null ) {
            log.debug("Creating new event router"); //$NON-NLS-1$
            this.cached = new ComponentEventRouter(compConfig);
        }
        return this.cached;
    }


    @Override
    public void close () throws JMSException {
        ComponentEventRouter c = this.cached;
        if ( c != null ) {
            this.cached = null;
            c.close();
        }
    }

}
