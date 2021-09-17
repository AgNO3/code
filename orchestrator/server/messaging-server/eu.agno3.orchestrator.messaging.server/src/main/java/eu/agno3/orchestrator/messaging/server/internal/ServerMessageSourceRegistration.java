/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.internal;


import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.config.ServerConfiguration;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class ServerMessageSourceRegistration {

    private ServiceRegistration<MessageSource> messageSourceRegistration;
    private ServerConfiguration config;


    @Reference
    protected synchronized void setServerConfig ( ServerConfiguration cfg ) {
        this.config = cfg;
    }


    protected synchronized void unsetServerConfig ( ServerConfiguration cfg ) {
        if ( this.config == cfg ) {
            this.config = null;
        }
    }


    @Activate
    protected synchronized void activate ( ComponentContext context ) {
        this.messageSourceRegistration = DsUtil.registerSafe(context, MessageSource.class, new ServerMessageSource(this.config.getServerId()), null);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext context ) {
        DsUtil.unregisterSafe(context, this.messageSourceRegistration);
        this.messageSourceRegistration = null;
    }
}
