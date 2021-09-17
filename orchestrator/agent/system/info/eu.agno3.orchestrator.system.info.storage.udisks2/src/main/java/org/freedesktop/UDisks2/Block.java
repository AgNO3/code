package org.freedesktop.UDisks2;
import java.util.List;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.UnixFD;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Block")
public interface Block extends DBusInterface
{

  public void AddConfigurationItem(Struct2 item, Map<String,Variant> options);
  public void RemoveConfigurationItem(Struct3 item, Map<String,Variant> options);
  public void UpdateConfigurationItem(Struct4 old_item, Struct5 new_item, Map<String,Variant> options);
  public List<Struct6> GetSecretConfiguration(Map<String,Variant> options);
  public void Format(String type, Map<String,Variant> options);
  public UnixFD OpenForBackup(Map<String,Variant> options);
  public UnixFD OpenForRestore(Map<String,Variant> options);
  public UnixFD OpenForBenchmark(Map<String,Variant> options);
  public void Rescan(Map<String,Variant> options);

}
