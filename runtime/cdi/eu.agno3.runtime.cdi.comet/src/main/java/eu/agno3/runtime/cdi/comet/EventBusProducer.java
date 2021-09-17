/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.10.2015 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.Produces;

import org.atmosphere.cpr.AtmosphereFramework;
import org.atmosphere.cpr.MetaBroadcaster;
import org.primefaces.push.EventBus;
import org.primefaces.push.impl.EventBusImpl;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
@Alternative
public class EventBusProducer {

    private EventBus eventBus;
    private MetaBroadcaster metaBroadcaster;


    @Produces
    @ApplicationScoped
    protected EventBus getEventBus () {
        if ( this.metaBroadcaster == null ) {
            throw new IllegalStateException("EventBus injected before configured"); //$NON-NLS-1$
        }
        if ( this.eventBus == null && this.metaBroadcaster != null ) {
            this.eventBus = new EventBusImpl(this.metaBroadcaster);

        }
        return this.eventBus;
    }


    /**
     * @param framework
     */
    public void configure ( AtmosphereFramework framework ) {
        this.metaBroadcaster = framework.metaBroadcaster();
    }
}
