/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.server.internal.router;


import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.messaging.server.AbstractServerEventRouter;
import eu.agno3.runtime.messaging.routing.EventRouter;


/**
 * @author mbechler
 * 
 */
@Component ( service = EventRouter.class, property = "scopeClass=eu.agno3.orchestrator.gui.msg.addressing.GuiEventScope", immediate = true )
public class GuiScopeRouter extends AbstractServerEventRouter {

}
