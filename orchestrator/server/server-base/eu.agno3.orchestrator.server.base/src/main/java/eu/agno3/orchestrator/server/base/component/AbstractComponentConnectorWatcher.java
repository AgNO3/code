/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.06.2014 by mbechler
 */
package eu.agno3.orchestrator.server.base.component;


import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.server.component.ComponentConfig;
import eu.agno3.orchestrator.server.component.ComponentConfigurationException;
import eu.agno3.orchestrator.server.component.ComponentConfigurationProvider;
import eu.agno3.orchestrator.server.component.ComponentConnectorWatcher;
import eu.agno3.orchestrator.server.component.ComponentLifecycleListener;
import eu.agno3.orchestrator.server.component.ComponentState;
import eu.agno3.runtime.util.detach.Detach;
import eu.agno3.runtime.util.detach.DetachedRunnable;


/**
 * @author mbechler
 * 
 * @param <T>
 */
public abstract class AbstractComponentConnectorWatcher <T extends ComponentConfig> implements ComponentConnectorWatcher {

    private static final Logger log = Logger.getLogger(AbstractComponentConnectorWatcher.class);

    private static final int MAX_NUM_THREADS = 3;

    private Map<@NonNull UUID, ComponentState> stateMap = new HashMap<>();
    private Map<@NonNull UUID, DateTime> lastPingMap = new HashMap<>();
    private Set<ComponentLifecycleListener<T>> lifecycleListeners = new HashSet<>();
    private ComponentConfigurationProvider<T> configProvider;

    private ExecutorService executor = Executors.newFixedThreadPool(MAX_NUM_THREADS, new ThreadFactory() {

        @Override
        public Thread newThread ( Runnable r ) {
            try {
                return Detach.runDetached(new DetachedRunnable<Thread>() {

                    @Override
                    public Thread run () throws Exception {
                        Thread t = new Thread(r, "Component lifecycle executor"); //$NON-NLS-1$
                        t.setContextClassLoader(null);
                        t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

                            @Override
                            public void uncaughtException ( Thread thrd, Throwable e ) {
                                getLog().error("Component lifecycle thread failed", e); //$NON-NLS-1$
                            }
                        });
                        return t;
                    };
                });
            }
            catch ( Exception e ) {
                getLog().error("Failed to create thread", e); //$NON-NLS-1$
                throw new IllegalStateException();
            }

        }
    });


    /**
     * @return the log
     */
    public static Logger getLog () {
        return log;
    }


    /**
     * 
     */
    public AbstractComponentConnectorWatcher () {
        super();
    }


    protected void bindLifecycleListener ( ComponentLifecycleListener<T> listener ) {
        synchronized ( this.lifecycleListeners ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Add lifecycle listener " + listener.getClass().getName()); //$NON-NLS-1$
            }
            this.lifecycleListeners.add(listener);
        }
    }


    protected void unbindLifecycleListener ( ComponentLifecycleListener<T> listener ) {
        synchronized ( this.lifecycleListeners ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Remove lifecycle listener " + listener.getClass().getName()); //$NON-NLS-1$
            }
            this.lifecycleListeners.remove(listener);
        }
    }


    protected void setConfigProvider ( ComponentConfigurationProvider<T> prov ) {
        this.configProvider = prov;
    }


    protected void unsetConfigProvider ( ComponentConfigurationProvider<T> prov ) {
        if ( this.configProvider == prov ) {
            this.configProvider = null;
        }
    }


    protected void setComponentState ( @NonNull UUID componentId, ComponentState state ) {
        ComponentState oldState = this.stateMap.get(componentId);
        if ( oldState == state ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Setting component %s state to %s", componentId, state)); //$NON-NLS-1$
        }

        this.stateMap.put(componentId, state);
        try {
            T cfg = this.configProvider.getConfiguration(componentId);

            synchronized ( this.lifecycleListeners ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Executing lifecycle listeners " + this.lifecycleListeners); //$NON-NLS-1$
                }

                for ( ComponentLifecycleListener<T> listener : this.lifecycleListeners ) {
                    this.executor.execute(new LifecycleNotifier<>(listener, state, cfg));
                }
            }
        }
        catch ( ComponentConfigurationException e ) {
            log.error("Failed to get agent configuration:", e); //$NON-NLS-1$
            return;
        }

    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentConnectorWatcher#getComponentConnectorState(java.util.UUID)
     */
    @Override
    public ComponentState getComponentConnectorState ( @NonNull UUID componentId ) {
        if ( !this.stateMap.containsKey(componentId) ) {
            return ComponentState.UNKNOWN;
        }

        return this.stateMap.get(componentId);
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentConnectorWatcher#getActiveComponentIds()
     */
    @Override
    public Set<@NonNull UUID> getActiveComponentIds () {
        return this.stateMap.keySet();
    }


    /**
     * 
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.server.component.ComponentConnectorWatcher#getLastPing(java.util.UUID)
     */
    @Override
    public DateTime getLastPing ( @NonNull UUID agentId ) {
        return this.lastPingMap.get(agentId);
    }


    protected void newComponent ( @NonNull UUID componentId ) {
        this.lastPingMap.put(componentId, new DateTime());
    }


    /**
     * 
     * @param componentId
     */
    public void connecting ( @NonNull UUID componentId ) {
        this.lastPingMap.put(componentId, new DateTime());
        this.setComponentState(componentId, ComponentState.CONNECTING);
        this.newComponent(componentId);
    }


    /**
     * 
     * @param componentId
     */
    public void connected ( @NonNull UUID componentId ) {
        this.lastPingMap.put(componentId, new DateTime());
        if ( !ComponentState.CONNECTING.equals(this.stateMap.get(componentId)) && !ComponentState.CONNECTED.equals(this.stateMap.get(componentId)) ) {
            log.error("CONNECTED event received in state " + this.stateMap.get(componentId)); //$NON-NLS-1$
            this.setComponentState(componentId, ComponentState.FAILURE);
            return;
        }

        this.setComponentState(componentId, ComponentState.CONNECTED);
    }


    /**
     * 
     * @param componentId
     */
    public void disconnecting ( @NonNull UUID componentId ) {
        this.lastPingMap.put(componentId, new DateTime());
        if ( ! ( ComponentState.CONNECTED.equals(this.stateMap.get(componentId)) || ComponentState.CONNECTING.equals(this.stateMap.get(componentId))
                || ComponentState.FAILURE.equals(this.stateMap.get(componentId)) ) ) {
            if ( this.stateMap.get(componentId) == null ) {
                this.setComponentState(componentId, ComponentState.DISCONNECTED);
                return;
            }

            log.error("DISCONNECTING event recieved in state " + this.stateMap.get(componentId)); //$NON-NLS-1$
            this.setComponentState(componentId, ComponentState.FAILURE);
            return;
        }

        this.setComponentState(componentId, ComponentState.DISCONNECTED);
    }


    /**
     * 
     * @param componentId
     */
    public void pinging ( @NonNull UUID componentId ) {
        ComponentState st = this.stateMap.get(componentId);
        if ( !ComponentState.CONNECTED.equals(st) ) {
            log.debug("PING event recieved in state " + st); //$NON-NLS-1$
        }

        this.setComponentState(componentId, ComponentState.CONNECTED);
        this.lastPingMap.put(componentId, new DateTime());
    }


    /**
     * @param componentId
     */
    public void timeout ( @NonNull UUID componentId ) {
        this.setComponentState(componentId, ComponentState.FAILURE);
    }

}