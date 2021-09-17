package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.PartitionTable")
public interface PartitionTable extends DBusInterface
{

  public DBusInterface CreatePartition(UInt64 offset, UInt64 size, String type, String name, Map<String,Variant> options);

}
