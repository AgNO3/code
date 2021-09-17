package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Loop")
public interface Loop extends DBusInterface
{

  public void Delete(Map<String,Variant> options);
  public void SetAutoclear(boolean value, Map<String,Variant> options);

}
