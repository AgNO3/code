/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 29, 2017 by mbechler
 */
package eu.agno3.orchestrator.server.app;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.config.model.base.server.context.DefaultServerServiceContext;
import eu.agno3.orchestrator.gui.server.EagerServicesActive;
import eu.agno3.runtime.http.service.webapp.WebAppDependencies;
import eu.agno3.runtime.ws.server.WebserviceEndpointInfo;


/**
 * @author mbechler
 *
 */
@Component ( service = WebAppDependencies.class, property = "instanceId=orchserver-gui" )
public class GuiWebAppDependencies implements WebAppDependencies {

    @Reference
    protected synchronized void bindServerContext ( DefaultServerServiceContext ssc ) {}


    protected synchronized void unbindServerContext ( DefaultServerServiceContext ssc ) {}


    @Reference
    protected synchronized void bindWebServiceEndpoint ( WebserviceEndpointInfo wsei ) {}


    protected synchronized void unbindWebServiceEndpoint ( WebserviceEndpointInfo wsei ) {}


    @Reference
    protected synchronized void bindEagerServicesActive ( EagerServicesActive eager ) {}


    protected synchronized void unbindEagerServicesActive ( EagerServicesActive eager ) {}
}
