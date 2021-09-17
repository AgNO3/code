/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.remote;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.quartz.DisallowConcurrentExecution;

import eu.agno3.orchestrator.gui.config.GuiConfig;
import eu.agno3.orchestrator.server.connector.impl.AbstractConnectionMonitor;
import eu.agno3.runtime.scheduler.TriggeredJob;


/**
 * @author mbechler
 * 
 */
@DisallowConcurrentExecution
@Component ( service = TriggeredJob.class, property = "jobType=eu.agno3.orchestrator.gui.connector.remote.GuiConnectionMonitor" )
public class GuiConnectionMonitor extends AbstractConnectionMonitor<GuiConfig> {

    @Reference
    protected synchronized void setServerConnector ( RemoteGuiConnector c ) {
        super.setServerConnector(c);
    }


    protected synchronized void unsetServerConnector ( RemoteGuiConnector c ) {
        super.unsetServerConnector(c);
    }

}
