package org.freedesktop.login1;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct3 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final DBusInterface b;
  public Struct3(String a, DBusInterface b)
  {
   this.a = a;
   this.b = b;
  }
}
