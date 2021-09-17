/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.init.systemd;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.freedesktop.dbus.DBus.Properties;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.systemd1.Manager;
import org.freedesktop.systemd1.Unit;

import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceState;


/**
 * @author mbechler
 * 
 */
public class SystemdUnitAdapter implements Service {

    private static final Logger log = Logger.getLogger(SystemdUnitAdapter.class);

    private static final Map<String, ServiceState> SERVICE_STATES = new HashMap<>();


    static {
        SERVICE_STATES.put("active", ServiceState.ACTIVE); //$NON-NLS-1$
        SERVICE_STATES.put("activating", ServiceState.ACTIVATING); //$NON-NLS-1$
        SERVICE_STATES.put("inactive", ServiceState.INACTIVE); //$NON-NLS-1$
        SERVICE_STATES.put("deactivating", ServiceState.DEACTIVATING); //$NON-NLS-1$
        SERVICE_STATES.put("failed", ServiceState.FAILED); //$NON-NLS-1$
    }

    private static final String UNIT_PROPERTIES = "org.freedesktop.systemd1.Unit"; //$NON-NLS-1$
    private org.freedesktop.systemd1.Unit s;
    private Properties p;
    private Manager m;
    private String unitId;


    protected SystemdUnitAdapter ( String unitId, Manager m, org.freedesktop.systemd1.Unit s, Properties props ) {
        this.unitId = unitId;
        this.m = m;
        this.s = s;
        this.p = props;
    }


    /**
     * @return the unit id
     */
    public String getId () {
        return this.unitId;
    }


    @Override
    public String getServiceName () {
        String id = this.getId();
        String unitName = id.substring(0, id.indexOf('.'));

        int indexOfAt = unitName.indexOf('@');
        if ( indexOfAt >= 0 ) {
            return unitName.substring(0, indexOfAt);
        }
        return unitName;
    }


    @Override
    public String getInstanceId () {
        String id = this.getId();
        String unitName = id.substring(0, id.indexOf('.'));

        int indexOfAt = unitName.indexOf('@');
        if ( indexOfAt >= 0 ) {
            return unitName.substring(indexOfAt + 1);
        }
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#getState()
     */
    @Override
    public ServiceState getState () {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            Properties props = this.p;
            if ( props == null ) {
                return ServiceState.INACTIVE;
            }
            String state = props.Get(UNIT_PROPERTIES, "ActiveState"); //$NON-NLS-1$
            return serviceStateFromString(state);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    private static ServiceState serviceStateFromString ( String state ) {
        ServiceState s = SERVICE_STATES.get(state);
        if ( s == null ) {
            return ServiceState.UNKNOWN;
        }
        return s;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#start()
     */
    @Override
    public boolean start () throws ServiceException {
        if ( this.getState() == ServiceState.ACTIVE ) {
            return false;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Starting service " + getId()); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            try {
                if ( this.s == null ) {
                    refresh();
                }
                this.s.Start(getMode());
                this.waitForRunningJobs();
            }
            catch ( DBusExecutionException e ) {
                throw new ServiceException("Failed to start service " + this.getId(), e); //$NON-NLS-1$
            }
            refresh();
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return true;
    }


    /**
     * @return
     */
    protected String getMode () {
        return "ignore-dependencies"; //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#stop()
     */
    @Override
    public boolean stop () throws ServiceException {
        if ( this.getState() == ServiceState.INACTIVE || this.getState() == ServiceState.FAILED ) {
            return false;
        }
        if ( log.isDebugEnabled() ) {
            log.debug("Stopping service " + getId()); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            try {
                this.s.Stop(getMode());
            }
            catch ( DBusExecutionException e ) {
                throw new ServiceException("Failed to stop service " + this.getId(), e); //$NON-NLS-1$
            }
            this.waitForRunningJobs();
            refresh();
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return true;
    }


    /**
     * Waits until all jobs associated with this unit are completed
     * 
     * @throws ServiceException
     */
    public void waitForRunningJobs () throws ServiceException {

        try {

            while ( getRunningJobId() != 0 ) {
                Thread.sleep(500);
                if ( log.isDebugEnabled() ) {
                    log.debug("Waiting for service " + getId()); //$NON-NLS-1$
                }
            }

        }
        catch ( InterruptedException e ) {
            throw new ServiceException("Interrupted while waiting for jobs to complete"); //$NON-NLS-1$
        }
    }


    private long getRunningJobId () {
        Object[] o = this.p.Get(UNIT_PROPERTIES, "Job"); //$NON-NLS-1$
        return ( (UInt32) o[ 0 ] ).longValue();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#reload()
     */
    @Override
    public boolean reload () throws ServiceException {
        if ( this.getState() != ServiceState.ACTIVE ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Reloading service " + getId()); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            try {
                this.s.ReloadOrTryRestart(getMode());
            }
            catch ( DBusExecutionException e ) {
                throw new ServiceException("Failed to reload service " + this.getId(), e); //$NON-NLS-1$
            }
            this.waitForRunningJobs();
            this.refresh();
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#restart()
     */
    @Override
    public boolean restart () throws ServiceException {
        if ( this.getState() != ServiceState.ACTIVE ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Restarting service " + getId()); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            try {
                this.s.TryRestart(getMode());
            }
            catch ( DBusExecutionException e ) {
                throw new ServiceException("Failed to restart service " + this.getId(), e); //$NON-NLS-1$
            }
            this.waitForRunningJobs();
            refresh();
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.service.Service#restartNoWait()
     */
    @Override
    public boolean restartNoWait () throws ServiceException {
        if ( this.getState() != ServiceState.ACTIVE ) {
            return false;
        }

        if ( log.isDebugEnabled() ) {
            log.debug("Restarting service " + getId()); //$NON-NLS-1$
        }

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            try {
                this.s.TryRestart(getMode());
            }
            catch ( DBusExecutionException e ) {
                throw new ServiceException("Failed to restart service " + this.getId(), e); //$NON-NLS-1$
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
        return true;
    }


    /**
     * 
     */
    private void refresh () {
        try {
            if ( log.isDebugEnabled() ) {
                log.debug("Refreshing service " + getId()); //$NON-NLS-1$
            }

            DBusInterface unit = this.m.GetUnit(getId());

            if ( ! ( unit instanceof Unit ) ) {
                log.warn("Returned unit not implementing unit"); //$NON-NLS-1$
                if ( unit != null && log.isDebugEnabled() ) {
                    for ( Class<?> cl : unit.getClass().getInterfaces() ) {
                        log.debug("Implements " + cl.getName()); //$NON-NLS-1$
                    }
                }
                return;
            }

            this.s = (Unit) unit;
            this.p = (Properties) this.s;
        }
        catch ( DBusExecutionException e ) {
            if ( "org.freedesktop.systemd1.NoSuchUnit".equals(e.getType()) ) { //$NON-NLS-1$
                if ( log.isDebugEnabled() ) {
                    log.debug("Unit was removed " + this.getId()); //$NON-NLS-1$
                }
                this.s = null;
                this.p = null;
                return;
            }

            log.warn(String.format("Failed to refresh service state %s: %s", this.getId(), e.getType()), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#enableOnBoot()
     */
    @Override
    public void enableOnBoot () throws ServiceException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

            if ( log.isDebugEnabled() ) {
                try {
                    throw new IllegalStateException();
                }
                catch ( IllegalStateException e ) {
                    log.debug("Enabling service " + getId(), e); //$NON-NLS-1$
                }
            }
            this.m.EnableUnitFiles(Arrays.asList(this.getId()), false, true);
        }
        catch ( DBusExecutionException e ) {
            throw new ServiceException("Failed to enable service " + this.getId(), e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.Service#disableOnBoot()
     */
    @Override
    public void disableOnBoot () throws ServiceException {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            if ( log.isDebugEnabled() ) {
                try {
                    throw new IllegalStateException();
                }
                catch ( IllegalStateException e ) {
                    log.debug("Disabling service " + getId(), e); //$NON-NLS-1$
                }
            }
            this.m.DisableUnitFiles(Arrays.asList(this.getId()), false);
        }
        catch ( DBusExecutionException e ) {
            throw new ServiceException("Failed to disable service " + this.getId(), e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }

    }

}
