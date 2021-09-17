package org.freedesktop.systemd1;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.systemd1.Unit")
public interface Unit extends DBusInterface
{

  public DBusInterface Start(String mode);
  public DBusInterface Stop(String mode);
  public DBusInterface Reload(String mode);
  public DBusInterface Restart(String mode);
  public DBusInterface TryRestart(String mode);
  public DBusInterface ReloadOrRestart(String mode);
  public DBusInterface ReloadOrTryRestart(String mode);
  public void Kill(String who, int signal);
  public void ResetFailed();
  public void SetProperties(boolean runtime, List<Properties> properties);

}
