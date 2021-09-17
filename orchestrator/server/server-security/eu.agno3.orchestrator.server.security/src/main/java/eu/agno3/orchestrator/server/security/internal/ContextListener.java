/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.10.2014 by mbechler
 */
package eu.agno3.orchestrator.server.security.internal;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.osgi.service.component.annotations.Component;


/**
 * Fake context listener to inject classloader
 * 
 * @author mbechler
 *
 */
@Component ( service = ServletContextListener.class, property = "context=api" )
public class ContextListener implements ServletContextListener {

    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed ( ServletContextEvent arg0 ) {}


    /**
     * {@inheritDoc}
     *
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized ( ServletContextEvent arg0 ) {}

}
