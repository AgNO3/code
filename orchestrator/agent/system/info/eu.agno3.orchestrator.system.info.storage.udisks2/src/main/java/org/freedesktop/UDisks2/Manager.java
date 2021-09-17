package org.freedesktop.UDisks2;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.UnixFD;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Manager")
public interface Manager extends DBusInterface
{

  public DBusInterface LoopSetup(UnixFD fd, Map<String,Variant> options);
  public DBusInterface MDRaidCreate(List<DBusInterface> blocks, String level, String name, UInt64 chunk, Map<String,Variant> options);

}
