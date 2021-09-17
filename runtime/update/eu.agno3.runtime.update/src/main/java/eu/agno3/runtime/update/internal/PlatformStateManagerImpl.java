/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.12.2014 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.eclipse.osgi.service.runnable.StartupMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.startlevel.FrameworkStartLevel;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.configloader.ReconfigurationListener;
import eu.agno3.runtime.jmx.MBean;
import eu.agno3.runtime.update.ApplicationStateProvider;
import eu.agno3.runtime.update.PlatformActivated;
import eu.agno3.runtime.update.PlatformState;
import eu.agno3.runtime.update.PlatformStateListener;
import eu.agno3.runtime.update.PlatformStateMXBean;
import eu.agno3.runtime.update.PlatformStateManager;
import eu.agno3.runtime.update.RefreshListener;
import eu.agno3.runtime.util.osgi.DsUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    PlatformStateManager.class, PlatformStateManagerImpl.class, RefreshListener.class, ReconfigurationListener.class, PlatformStateMXBean.class,
    MBean.class
}, immediate = true, configurationPid = PlatformStateManagerImpl.PID, property = {
    "objectName=eu.agno3.runtime.update:type=PlatformState"
} )
public class PlatformStateManagerImpl
        implements PlatformStateManager, FrameworkListener, RefreshListener, ReconfigurationListener, PlatformStateMXBean, MBean {

    /**
     * 
     */
    private static final int RECONFIGURE_TIMEOUT = 5;

    /**
     * 
     */
    public static final String PID = "platformManager"; //$NON-NLS-1$

    private static final long START_TIME = System.currentTimeMillis();

    private static final Logger log = Logger.getLogger(PlatformStateManagerImpl.class);
    private ComponentContext componentContext;
    private int targetStartLevel = 10;
    private boolean startedOnce;
    private PlatformState currentState = PlatformState.BOOTING;
    private PlatformState lastState = PlatformState.BOOTING;

    private Set<PlatformStateListener> listeners = new HashSet<>();
    private ApplicationStateProvider appStateProvider;

    private ServiceRegistration<PlatformActivated> platformActivatedRegistration;

    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();


    @Reference ( cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindListener ( PlatformStateListener l ) {
        this.listeners.add(l);
        l.stateChanged(this.currentState);
    }


    protected synchronized void unbindListener ( PlatformStateListener l ) {
        this.listeners.remove(l);
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindApplicationStateProvider ( ApplicationStateProvider asp ) {
        this.appStateProvider = asp;
        if ( this.currentState != PlatformState.RECONFIGURE && this.currentState != PlatformState.UPDATING ) {
            PlatformState status = this.appStateProvider.getStatus();
            if ( status == PlatformState.STARTED || status == PlatformState.WARNING || this.startedOnce ) {
                setState(status);
            }
        }
        else if ( this.startedOnce ) {
            PlatformState status = this.appStateProvider.getStatus();
            if ( status == PlatformState.STARTED || status == PlatformState.WARNING ) {
                setState(status);
            }
            else {
                this.executor.schedule(new RefreshState(), RECONFIGURE_TIMEOUT, TimeUnit.SECONDS);
            }
        }
    }


    protected synchronized void unbindApplicationStateProvider ( ApplicationStateProvider asp ) {
        if ( this.appStateProvider == asp ) {
            this.appStateProvider = null;
            if ( this.currentState != PlatformState.STOPPING && this.startedOnce ) {
                setState(this.currentState == PlatformState.UPDATING ? PlatformState.UPDATING : PlatformState.RECONFIGURE);
            }
        }
    }


    @Reference ( cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC )
    protected synchronized void bindStartupMonitor ( StartupMonitor sm ) {
        // unbind only
    }


    protected synchronized void unbindStartupMonitor ( StartupMonitor sm ) {
        setState(PlatformState.STOPPING);
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        this.componentContext = ctx;

        String targetLevelSpec = (String) ctx.getProperties().get("targetLevel"); //$NON-NLS-1$
        if ( !StringUtils.isBlank(targetLevelSpec) ) {
            this.targetStartLevel = Integer.parseInt(targetLevelSpec.trim());
        }

        ctx.getBundleContext().addFrameworkListener(this);
    }


    @Deactivate
    protected synchronized void deactivate ( ComponentContext ctx ) {
        if ( this.platformActivatedRegistration != null ) {
            DsUtil.unregisterSafe(this.componentContext, this.platformActivatedRegistration);
            this.platformActivatedRegistration = null;
        }
        if ( this.componentContext != null ) {
            ctx.getBundleContext().removeFrameworkListener(this);
            this.componentContext = null;
        }
    }


    /**
     * @param updating
     */
    private synchronized void setState ( PlatformState state ) {
        if ( this.currentState == PlatformState.STOPPING ) {
            return;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Setting state to " + state); //$NON-NLS-1$
        }

        if ( state == PlatformState.STARTED || state == PlatformState.WARNING ) {
            if ( !this.startedOnce ) {
                log.info(String.format("Startup took %d ms", System.currentTimeMillis() - START_TIME)); //$NON-NLS-1$
            }
            this.startedOnce = true;
            this.platformActivatedRegistration = DsUtil
                    .registerSafe(this.componentContext, PlatformActivated.class, new PlatformActivatedMarker(), null);
        }

        this.notifyListeners(state);

        this.lastState = this.currentState;
        this.currentState = state;
    }


    @Override
    public void refreshAppState () {
        ApplicationStateProvider asp = this.appStateProvider;
        if ( asp != null && this.currentState != PlatformState.RECONFIGURE && this.currentState != PlatformState.UPDATING ) {
            PlatformState ps = asp.getStatus();
            if ( ps != this.currentState ) {
                setState(ps);
            }
        }
        else if ( this.startedOnce ) {
            setState(PlatformState.FAILED);
        }
    }


    @Override
    public String getState () {
        Bundle sysBundle = this.componentContext.getBundleContext().getBundle(0);
        FrameworkStartLevel startLevel = sysBundle.adapt(FrameworkStartLevel.class);
        if ( this.startedOnce && startLevel.getStartLevel() == this.targetStartLevel ) {
            if ( this.currentState == PlatformState.STARTED ) {
                if ( this.appStateProvider == null ) {
                    return PlatformState.FAILED.name();
                }
                return this.appStateProvider.getStatus().name();
            }
        }
        return this.currentState.name();
    }


    /**
     * @param state
     */
    private void notifyListeners ( PlatformState state ) {
        for ( PlatformStateListener l : this.listeners ) {
            l.stateChanged(state);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.osgi.framework.FrameworkListener#frameworkEvent(org.osgi.framework.FrameworkEvent)
     */
    @Override
    public void frameworkEvent ( FrameworkEvent ev ) {
        if ( ev.getType() == FrameworkEvent.STARTLEVEL_CHANGED ) {
            Bundle sysBundle = this.componentContext.getBundleContext().getBundle(0);
            FrameworkStartLevel startLevel = sysBundle.adapt(FrameworkStartLevel.class);

            if ( log.isDebugEnabled() ) {
                log.debug("Startlevel changed to " + startLevel.getStartLevel()); //$NON-NLS-1$
            }

            if ( this.startedOnce && startLevel.getStartLevel() < this.targetStartLevel ) {
                this.setState(PlatformState.STOPPING);
            }
            else {
                // delay until ApplicationStateProvider is bound
                this.executor.schedule(new PlatformStateTimeout(), 30, TimeUnit.SECONDS);
            }
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesRefreshed()
     */
    @Override
    public void bundlesRefreshed () throws Exception {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#startBundleUpdate()
     */
    @Override
    public void startBundleUpdate () {
        this.setState(PlatformState.UPDATING);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesUpdated()
     */
    @Override
    public void bundlesUpdated () throws Exception {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.update.RefreshListener#bundlesStarted()
     */
    @Override
    public void bundlesStarted () {
        ApplicationStateProvider asp = this.appStateProvider;
        if ( this.currentState == PlatformState.RECONFIGURE ) {
            return;
        }
        else if ( asp != null ) {
            this.setState(asp.getStatus());
            return;
        }

        log.debug("Application state provider is not yet available"); //$NON-NLS-1$
        this.executor.schedule(new PlatformStateTimeout(), 5, TimeUnit.SECONDS);
    }


    /**
     * 
     */
    void startupTimeout () {
        if ( this.appStateProvider != null ) {
            this.setState(this.appStateProvider.getStatus());
            return;
        }

        if ( this.currentState != PlatformState.BOOTING ) {
            return;
        }

        log.error("Bundles are started but application is not satisfied"); //$NON-NLS-1$
        this.setState(PlatformState.FAILED);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.ReconfigurationListener#startReconfigure()
     */
    @Override
    public void startReconfigure () {
        Bundle sysBundle = this.componentContext.getBundleContext().getBundle(0);
        FrameworkStartLevel startLevel = sysBundle.adapt(FrameworkStartLevel.class);
        if ( this.startedOnce && startLevel.getStartLevel() == this.targetStartLevel ) {
            if ( this.currentState != PlatformState.BOOTING ) {
                setState(PlatformState.RECONFIGURE);
            }
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.configloader.ReconfigurationListener#finishReconfigure()
     */
    @Override
    public void finishReconfigure () {
        Bundle sysBundle = this.componentContext.getBundleContext().getBundle(0);
        FrameworkStartLevel startLevel = sysBundle.adapt(FrameworkStartLevel.class);
        if ( this.startedOnce && startLevel.getStartLevel() == this.targetStartLevel ) {
            if ( this.currentState == PlatformState.RECONFIGURE ) {
                ApplicationStateProvider asp = this.appStateProvider;
                if ( asp != null ) {
                    PlatformState st = asp.getStatus();
                    if ( st == PlatformState.STARTED || st == PlatformState.WARNING ) {
                        setState(st);
                    }
                    else {
                        this.executor.schedule(new RefreshState(), RECONFIGURE_TIMEOUT, TimeUnit.SECONDS);
                    }
                }
                else {
                    setState(this.lastState);
                }
            }
        }
    }

    /**
     * @author mbechler
     *
     */
    public class RefreshState implements Runnable {

        @Override
        public void run () {
            refreshAppState();
        }
    }

    /**
     * @author mbechler
     *
     */
    public class PlatformStateTimeout implements Runnable {

        /**
         * {@inheritDoc}
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run () {
            startupTimeout();
        }

    }
}
