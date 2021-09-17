package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Partition")
public interface Partition extends DBusInterface
{

  public void SetType(String type, Map<String,Variant> options);
  public void SetName(String name, Map<String,Variant> options);
  public void SetFlags(UInt64 flags, Map<String,Variant> options);
  public void Delete(Map<String,Variant> options);

}
