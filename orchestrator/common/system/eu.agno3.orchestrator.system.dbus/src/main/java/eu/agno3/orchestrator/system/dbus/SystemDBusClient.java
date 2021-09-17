/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dbus;


import org.freedesktop.dbus.CallbackHandler;
import org.freedesktop.dbus.DBusAsyncReply;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;


/**
 * @author mbechler
 * 
 */
public interface SystemDBusClient {

    /**
     * @param signal
     * @throws DBusException
     */
    void sendSignal ( DBusSignal signal ) throws DBusException;


    /**
     * @param type
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void removeSigHandler ( Class<T> type, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param type
     * @param object
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void removeSigHandler ( Class<T> type, DBusInterface object, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param busname
     * @param objectpath
     * @return the object
     * @throws DBusException
     */
    DBusInterface getRemoteObject ( String busname, String objectpath ) throws DBusException;


    /**
     * @param type
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void addSigHandler ( Class<T> type, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param type
     * @param object
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void addSigHandler ( Class<T> type, DBusInterface object, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param busname
     * @param objectpath
     * @param type
     * @return the object
     * @throws DBusException
     */
    <I extends DBusInterface> I getRemoteObject ( String busname, String objectpath, Class<I> type ) throws DBusException;


    /**
     * @param object
     * @param m
     * @param callback
     * @param parameters
     * @throws DBusException
     */
    <A> void callWithCallback ( DBusInterface object, String m, CallbackHandler<A> callback, Object... parameters ) throws DBusException;


    /**
     * @param busname
     * @param objectpath
     * @param type
     * @param autostart
     * @return the object
     * @throws DBusException
     */
    <I extends DBusInterface> I getRemoteObject ( String busname, String objectpath, Class<I> type, boolean autostart ) throws DBusException;


    /**
     * @param object
     * @param m
     * @param parameters
     * @return a call handle
     * @throws DBusException
     */
    DBusAsyncReply<?> callMethodAsync ( DBusInterface object, String m, Object... parameters ) throws DBusException;


    /**
     * @param type
     * @param source
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void removeSigHandler ( Class<T> type, String source, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param type
     * @param source
     * @param object
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void removeSigHandler ( Class<T> type, String source, DBusInterface object, DBusSigHandler<T> handler )
            throws DBusException;


    /**
     * @param type
     * @param source
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void addSigHandler ( Class<T> type, String source, DBusSigHandler<T> handler ) throws DBusException;


    /**
     * @param type
     * @param source
     * @param object
     * @param handler
     * @throws DBusException
     */
    <T extends DBusSignal> void addSigHandler ( Class<T> type, String source, DBusInterface object, DBusSigHandler<T> handler ) throws DBusException;

}