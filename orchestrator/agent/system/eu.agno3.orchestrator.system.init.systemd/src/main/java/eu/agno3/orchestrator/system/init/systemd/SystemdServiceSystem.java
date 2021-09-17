/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.init.systemd;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.freedesktop.dbus.DBus.Properties;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.exceptions.DBusExecutionException;
import org.freedesktop.systemd1.Manager;
import org.freedesktop.systemd1.Struct1;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.base.SystemService;
import eu.agno3.orchestrator.system.base.SystemServiceType;
import eu.agno3.orchestrator.system.base.service.Service;
import eu.agno3.orchestrator.system.base.service.ServiceException;
import eu.agno3.orchestrator.system.base.service.ServiceSystem;
import eu.agno3.orchestrator.system.dbus.SystemDBusClient;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ServiceSystem.class, SystemService.class
} )
@SystemServiceType ( ServiceSystem.class )
public class SystemdServiceSystem implements ServiceSystem {

    private static final String SYSTEMD_BUSNAME = "org.freedesktop.systemd1"; //$NON-NLS-1$
    private static final String SYSTEMD_ROOT_PATH = "/org/freedesktop/systemd1"; //$NON-NLS-1$

    private SystemDBusClient dbus;


    @Activate
    protected void activate ( ComponentContext ctx ) throws DBusException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, Manager.class);
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext ctx ) {

    }


    @Reference
    protected synchronized void setDBusClient ( SystemDBusClient cl ) {
        this.dbus = cl;
    }


    protected synchronized void unsetDBusClient ( SystemDBusClient cl ) {
        if ( this.dbus == cl ) {
            this.dbus = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws ServiceException
     * 
     * @see eu.agno3.orchestrator.system.base.service.ServiceSystem#getServices()
     */
    @Override
    public Collection<Service> getServices () throws ServiceException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

        try {
            Manager remoteObject = getManager();

            List<Service> res = new ArrayList<>();
            List<Struct1> units = remoteObject.ListUnits();

            for ( Struct1 unit : units ) {
                String unitId = unit.a;
                DBusInterface unitObj = unit.g;
                if ( unitObj instanceof org.freedesktop.systemd1.Service ) {
                    res.add(new SystemdUnitAdapter(unitId, remoteObject, (org.freedesktop.systemd1.Unit) unitObj, (Properties) unitObj));
                }
            }

            return res;
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    private Manager getManager () throws ServiceException {
        try {
            return this.dbus.getRemoteObject(SYSTEMD_BUSNAME, SYSTEMD_ROOT_PATH, Manager.class);
        }
        catch ( DBusException e ) {
            throw new ServiceException("Failed to get service manager", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.ServiceSystem#getService(java.lang.String)
     */
    @Override
    public Service getService ( String name ) throws ServiceException {

        if ( name.indexOf('.') >= 0 ) {
            return this.getUnit(name, false);
        }

        return this.getUnit(String.format("%s.service", name), false); //$NON-NLS-1$

    }


    /**
     * @param format
     * @return
     */
    private Service getUnit ( String unitId, boolean instance ) throws ServiceException {

        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        try {
            DBusInterface unit = this.getManager().LoadUnit(unitId);
            return new SystemdUnitAdapter(unitId, this.getManager(), (org.freedesktop.systemd1.Unit) unit, (Properties) unit);
        }
        catch ( DBusExecutionException e ) {
            throw new ServiceException("Could not get unit " + unitId, e); //$NON-NLS-1$
        }
        finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.service.ServiceSystem#createInstance(java.lang.String, java.lang.String)
     */
    @Override
    public Service createInstance ( String name, String instance ) throws ServiceException {
        Service s = this.getService(name, instance);
        s.start();
        return s;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.service.ServiceSystem#getService(java.lang.String, java.lang.String)
     */
    @Override
    public Service getService ( String name, String instance ) throws ServiceException {
        if ( instance == null ) {
            return this.getService(name);
        }

        String serviceName;
        String type;

        int indexOfNameSep = name.indexOf('.');
        if ( indexOfNameSep >= 0 ) {
            serviceName = name.substring(0, indexOfNameSep);
            type = name.substring(indexOfNameSep + 1);
        }
        else {
            serviceName = name;
            type = "service"; //$NON-NLS-1$
        }

        return this.getUnit(String.format("%s@%s.%s", serviceName, instance, type), true); //$NON-NLS-1$
    }

}
