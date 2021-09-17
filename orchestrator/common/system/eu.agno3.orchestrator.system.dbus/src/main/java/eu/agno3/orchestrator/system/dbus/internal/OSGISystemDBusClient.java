/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dbus.internal;


import java.security.AccessController;
import java.security.PrivilegedAction;

import org.freedesktop.dbus.CallbackHandler;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.system.dbus.SystemDBusClient;
import eu.agno3.runtime.util.classloading.BundleDelegatingClassLoader;


/**
 * Per bundle dbus client proxy
 * 
 * @author mbechler
 * 
 */
@Component ( service = SystemDBusClient.class, servicefactory = true )
public class OSGISystemDBusClient implements SystemDBusClient {

    private DBUSSystemConnectionHolder connHolder;
    private ClassLoader cl;


    @Activate
    protected synchronized void activate ( final ComponentContext context ) {
        this.cl = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            @Override
            public ClassLoader run () {
                return new BundleDelegatingClassLoader(context.getUsingBundle());
            }

        });
    }


    @Deactivate
    protected synchronized void deactivate () {
        this.cl = null;
    }


    @Reference
    protected synchronized void setConnHolder ( DBUSSystemConnectionHolder holder ) {
        this.connHolder = holder;
    }


    protected synchronized void unsetConnHolder ( DBUSSystemConnectionHolder holder ) {
        if ( this.connHolder == holder ) {
            this.connHolder = null;
        }
    }


    private ClassLoader before () {
        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.cl);
        return oldTCCL;
    }


    @SuppressWarnings ( "static-method" )
    private void after ( ClassLoader old ) {
        Thread.currentThread().setContextClassLoader(old);
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DBusException
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#sendSignal(org.freedesktop.dbus.DBusSignal)
     */
    @Override
    public void sendSignal ( DBusSignal signal ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().sendSignal(signal);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#removeSigHandler(java.lang.Class,
     *      org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void removeSigHandler ( Class<T> type, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().removeSigHandler(type, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#removeSigHandler(java.lang.Class,
     *      org.freedesktop.dbus.DBusInterface, org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void removeSigHandler ( Class<T> type, DBusInterface object, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().removeSigHandler(type, object, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#getRemoteObject(java.lang.String, java.lang.String)
     */
    @Override
    public DBusInterface getRemoteObject ( String busname, String objectpath ) throws DBusException {
        ClassLoader old = this.before();
        try {
            return this.connHolder.getConnection().getRemoteObject(busname, objectpath);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#addSigHandler(java.lang.Class,
     *      org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void addSigHandler ( Class<T> type, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().addSigHandler(type, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#addSigHandler(java.lang.Class,
     *      org.freedesktop.dbus.DBusInterface, org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void addSigHandler ( Class<T> type, DBusInterface object, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().addSigHandler(type, object, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#getRemoteObject(java.lang.String, java.lang.String,
     *      java.lang.Class)
     */
    @Override
    public <I extends DBusInterface> I getRemoteObject ( String busname, String objectpath, Class<I> type ) throws DBusException {
        ClassLoader old = this.before();
        try {
            return this.connHolder.getConnection().getRemoteObject(busname, objectpath, type);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DBusException
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#callWithCallback(org.freedesktop.dbus.DBusInterface,
     *      java.lang.String, org.freedesktop.dbus.CallbackHandler, java.lang.Object[])
     */
    @Override
    public <A> void callWithCallback ( DBusInterface object, String m, CallbackHandler<A> callback, Object... parameters ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().callWithCallback(object, m, callback, parameters);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#getRemoteObject(java.lang.String, java.lang.String,
     *      java.lang.Class, boolean)
     */
    @Override
    public <I extends DBusInterface> I getRemoteObject ( String busname, String objectpath, Class<I> type, boolean autostart ) throws DBusException {
        ClassLoader old = this.before();
        try {
            return this.connHolder.getConnection().getRemoteObject(busname, objectpath, type, autostart);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @throws DBusException
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#callMethodAsync(org.freedesktop.dbus.DBusInterface,
     *      java.lang.String, java.lang.Object[])
     */
    @Override
    public DBusAsyncReply<?> callMethodAsync ( DBusInterface object, String m, Object... parameters ) throws DBusException {
        ClassLoader old = this.before();
        try {
            return this.connHolder.getConnection().callMethodAsync(object, m, parameters);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#removeSigHandler(java.lang.Class, java.lang.String,
     *      org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void removeSigHandler ( Class<T> type, String source, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().removeSigHandler(type, source, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#removeSigHandler(java.lang.Class, java.lang.String,
     *      org.freedesktop.dbus.DBusInterface, org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void removeSigHandler ( Class<T> type, String source, DBusInterface object, DBusSigHandler<T> handler )
            throws DBusException {
        ClassLoader old = this.before();
        try {

            this.connHolder.getConnection().removeSigHandler(type, source, object, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#addSigHandler(java.lang.Class, java.lang.String,
     *      org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void addSigHandler ( Class<T> type, String source, DBusSigHandler<T> handler ) throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().addSigHandler(type, source, handler);
        }
        finally {
            this.after(old);
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.dbus.SystemDBusClient#addSigHandler(java.lang.Class, java.lang.String,
     *      org.freedesktop.dbus.DBusInterface, org.freedesktop.dbus.DBusSigHandler)
     */
    @Override
    public <T extends DBusSignal> void addSigHandler ( Class<T> type, String source, DBusInterface object, DBusSigHandler<T> handler )
            throws DBusException {
        ClassLoader old = this.before();
        try {
            this.connHolder.getConnection().addSigHandler(type, source, object, handler);
        }
        finally {
            this.after(old);
        }
    }

}
