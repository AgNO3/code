package eu.agno3.runtime.webbeans;


import javax.servlet.ServletContextEvent;

import org.apache.webbeans.config.WebBeansFinder;
import org.apache.webbeans.servlet.WebBeansConfigurationListener;


/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2013 by mbechler
 */

/**
 * @author mbechler
 * 
 */
public class ProperWebBeansConfigurationListener extends WebBeansConfigurationListener {

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.webbeans.servlet.WebBeansConfigurationListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized ( ServletContextEvent event ) {
        super.contextInitialized(event);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.webbeans.servlet.WebBeansConfigurationListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed ( ServletContextEvent event ) {
        super.contextDestroyed(event);
        WebBeansFinder.clearInstances(Thread.currentThread().getContextClassLoader());
    }
}
