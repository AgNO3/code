/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.12.2015 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import org.apache.log4j.Logger;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentLifecycleListener;
import eu.agno3.orchestrator.server.component.ComponentState;


/**
 * @author mbechler
 * @param <T>
 *
 */
public class LifecycleNotifier <T extends ComponentConfig> implements Runnable {

    private static final Logger log = Logger.getLogger(LifecycleNotifier.class);

    private final ComponentLifecycleListener<T> listener;
    private final ComponentState state;
    private final T cfg;


    /**
     * @param listener
     * @param state
     * @param cfg
     */
    public LifecycleNotifier ( ComponentLifecycleListener<T> listener, ComponentState state, T cfg ) {
        this.listener = listener;
        this.cfg = cfg;
        this.state = state;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Starting component lifecycle listener " + this.listener.getClass().getName()); //$NON-NLS-1$
            }

            switch ( this.state ) {
            case CONNECTED:
                this.listener.connected(this.cfg);
                break;
            case DISCONNECTED:
                this.listener.disconnecting(this.cfg);
                break;
            case CONNECTING:
                this.listener.connecting(this.cfg);
                break;
            case FAILURE:
                this.listener.failed(this.cfg);
                break;
            default:
                break;
            }

            if ( log.isDebugEnabled() ) {
                log.debug("Finished component lifecycle listener " + this.listener.getClass().getName()); //$NON-NLS-1$
            }
        }
        catch ( Exception e ) {
            log.error("Failed to run component lifecycle listener", e); //$NON-NLS-1$
        }
    }

}
