package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.UDisks2.Encrypted")
public interface Encrypted extends DBusInterface
{

  public DBusInterface Unlock(String passphrase, Map<String,Variant> options);
  public void Lock(Map<String,Variant> options);
  public void ChangePassphrase(String passphrase, String new_passphrase, Map<String,Variant> options);

}
