/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.dbus.internal;


import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.exceptions.DBusException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;


/**
 * @author mbechler
 * 
 */
@Component ( service = DBUSSystemConnectionHolder.class )
public class DBUSSystemConnectionHolder {

    private DBusConnection conn;


    @Activate
    protected void activate ( ComponentContext context ) {

    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( this.conn != null ) {
            this.conn.disconnect();
        }
        this.conn = null;
    }


    /**
     * @return the conn
     * @throws DBusException
     */
    public DBusConnection getConnection () throws DBusException {
        DBusConnection c = DBusConnection.getConnection(DBusConnection.SYSTEM);
        this.conn = c;
        return c;
    }

}
