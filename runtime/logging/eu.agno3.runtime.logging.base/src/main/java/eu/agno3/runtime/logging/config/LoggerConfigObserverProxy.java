/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.06.2013 by mbechler
 */
package eu.agno3.runtime.logging.config;


import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class LoggerConfigObserverProxy implements Observer {

    private static final Logger log = Logger.getLogger(LoggerConfigObserverProxy.class);
    private LoggerConfigObserver observer;


    /**
     * @param obs
     *            Delegate observer
     */
    public LoggerConfigObserverProxy ( LoggerConfigObserver obs ) {
        this.observer = obs;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
     */
    @Override
    public void update ( Observable o, Object arg ) {
        log.trace("Update called from " + o.getClass().getName()); //$NON-NLS-1$
        if ( o instanceof LoggerConfigurationSource ) {
            LoggerConfigurationSource source = (LoggerConfigurationSource) o;
            this.observer.configurationUpdated(source);
        }
    }

}
