/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.storage.internal;


import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.ObjectPath;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;


/**
 * @author mbechler
 *
 */
public interface DBUSReferenceResolver {

    /**
     * Get a proper proxy for a property referenced object
     * 
     * @param ref
     * @param variant
     * @param allObjects
     * @return the resolved object
     * @throws DBusException
     */
    DBusInterface resolveReferencedObject ( DBusInterface ref, Variant<ObjectPath> variant,
            Map<DBusInterface, Map<String, Map<String, Variant<?>>>> allObjects ) throws DBusException;

}