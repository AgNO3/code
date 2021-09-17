/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.11.2013 by mbechler
 */
package eu.agno3.runtime.cdi.bootstrap;


import javax.servlet.ServletContextEvent;

import org.apache.webbeans.servlet.WebBeansConfigurationListener;


/**
 * @author mbechler
 * 
 */
public class TCCLWebBeansConfigurationListener extends WebBeansConfigurationListener {

    /**
     * {@inheritDoc}
     * 
     * @see org.apache.webbeans.servlet.WebBeansConfigurationListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized ( ServletContextEvent arg0 ) {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            super.contextInitialized(arg0);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.webbeans.servlet.WebBeansConfigurationListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed ( ServletContextEvent event ) {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            super.contextDestroyed(event);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }
}
