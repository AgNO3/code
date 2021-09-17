/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2016 by mbechler
 */
package eu.agno3.orchestrator.gui.connector.remote;


import javax.jms.XAConnectionFactory;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.server.connector.ServerConnector;
import eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger;
import eu.agno3.runtime.update.PlatformActivated;


/**
 * @author mbechler
 *
 */
@Component ( service = GuiConnectedTrigger.class, immediate = true )
public class GuiConnectedTrigger extends ComponentConnectedTrigger {

    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#activate(org.osgi.service.component.ComponentContext)
     */
    @Activate
    @Override
    protected synchronized void activate ( ComponentContext ctx ) {
        super.activate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#deactivate(org.osgi.service.component.ComponentContext)
     */
    @Deactivate
    @Override
    protected synchronized void deactivate ( ComponentContext ctx ) {
        super.deactivate(ctx);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#setActivated(eu.agno3.runtime.update.PlatformActivated)
     */
    @Override
    @Reference
    protected synchronized void setActivated ( PlatformActivated pa ) {
        super.setActivated(pa);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#unsetActivated(eu.agno3.runtime.update.PlatformActivated)
     */
    @Override
    protected synchronized void unsetActivated ( PlatformActivated pa ) {
        super.unsetActivated(pa);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#setServerConnector(eu.agno3.orchestrator.server.connector.ServerConnector)
     */
    @Reference
    @Override
    protected synchronized void setServerConnector ( ServerConnector<?> sc ) {
        // TODO Auto-generated method stub
        super.setServerConnector(sc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#unsetServerConnector(eu.agno3.orchestrator.server.connector.ServerConnector)
     */
    @Override
    protected synchronized void unsetServerConnector ( ServerConnector<?> sc ) {
        super.unsetServerConnector(sc);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#setXAConnection(javax.jms.XAConnection)
     */
    @Reference
    @Override
    protected synchronized void setXAConnection ( XAConnectionFactory c ) {
        super.setXAConnection(c);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.server.connector.impl.ComponentConnectedTrigger#unsetXAConnection(javax.jms.XAConnection)
     */
    @Override
    protected synchronized void unsetXAConnection ( XAConnectionFactory c ) {
        super.unsetXAConnection(c);
    }
}
