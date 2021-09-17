/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.logind;


import org.freedesktop.dbus.exceptions.DBusException;

import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.dbus.SystemDBusClient;


/**
 * @author mbechler
 * 
 */
public class Logind {

    private static final String LOGIND_BUSNAME = "org.freedesktop.login1"; //$NON-NLS-1$
    private static final String LOGIND_ROOT_PATH = "/org/freedesktop/login1"; //$NON-NLS-1$
    private SystemDBusClient dbus;


    /**
     * @param cl
     */
    public Logind ( SystemDBusClient cl ) {
        this.dbus = cl;
    }


    private org.freedesktop.login1.Manager getLogindManager () throws DBusException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            return this.dbus.getRemoteObject(LOGIND_BUSNAME, LOGIND_ROOT_PATH, org.freedesktop.login1.Manager.class);
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    /**
     * Reboot the system
     * 
     * @throws ServiceException
     */
    public void reboot () throws ServiceException {
        try {
            this.getLogindManager().Reboot(false);
        }
        catch ( DBusException e ) {
            throw new ServiceException("Failed to trigger system reboot", e); //$NON-NLS-1$
        }
    }


    /**
     * Shutdown the system
     * 
     * @throws ServiceException
     */
    public void shutdown () throws ServiceException {
        try {
            this.getLogindManager().PowerOff(false);
        }
        catch ( DBusException e ) {
            throw new ServiceException("Failed to trigger system shutdown", e); //$NON-NLS-1$
        }
    }
}
