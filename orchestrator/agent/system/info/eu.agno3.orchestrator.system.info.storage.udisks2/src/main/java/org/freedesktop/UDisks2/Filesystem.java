package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Filesystem")
public interface Filesystem extends DBusInterface
{

  public void SetLabel(String label, Map<String,Variant> options);
  public String Mount(Map<String,Variant> options);
  public void Unmount(Map<String,Variant> options);

}
