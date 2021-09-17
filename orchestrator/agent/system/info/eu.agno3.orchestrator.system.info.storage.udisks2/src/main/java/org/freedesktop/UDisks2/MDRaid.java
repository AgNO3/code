package org.freedesktop.UDisks2;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.MDRaid")
public interface MDRaid extends DBusInterface
{

  public void Start(Map<String,Variant> options);
  public void Stop(Map<String,Variant> options);
  public void RemoveDevice(DBusInterface device, Map<String,Variant> options);
  public void AddDevice(DBusInterface device, Map<String,Variant> options);
  public void SetBitmapLocation(List<Byte> value, Map<String,Variant> options);
  public void RequestSyncAction(String sync_action, Map<String,Variant> options);

}
