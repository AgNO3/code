/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.connector;


import java.lang.reflect.UndeclaredThrowableException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.gui.connector.ws.GuiWebServiceException;
import eu.agno3.orchestrator.gui.connector.ws.GuiWsClientFactory;
import eu.agno3.runtime.cdi.Eager;


/**
 * @author mbechler
 * 
 */
@ApplicationScoped
@Default
@Eager
public class ServerServiceProvider {

    @Inject
    @OsgiService ( dynamic = true, timeout = 10000 )
    private GuiWsClientFactory wsClientFactory;


    @PostConstruct
    protected void init () {
        this.wsClientFactory.createAllServices();
    }


    /**
     * 
     * @param sei
     * @return the specified service
     * @throws GuiWebServiceException
     */
    public <T> T getService ( Class<T> sei ) throws GuiWebServiceException {
        try {
            return this.wsClientFactory.createService(sei);
        }
        catch ( UndeclaredThrowableException e ) {
            throw new GuiWebServiceException("Failed to get service", e); //$NON-NLS-1$
        }
    }
}
