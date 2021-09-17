package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Job")
public interface Job extends DBusInterface
{
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class Completed extends DBusSignal
   {
      public final boolean success;
      public final String message;
      public Completed(String path, boolean success, String message) throws DBusException
      {
         super(path, success, message);
         this.success = success;
         this.message = message;
      }
   }

  public void Cancel(Map<String,Variant> options);

}
