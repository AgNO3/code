package org.freedesktop.login1;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.UInt32;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct2 extends Struct
{
   @Position(0)
   public final UInt32 a;
   @Position(1)
   public final String b;
   @Position(2)
   public final DBusInterface c;
  public Struct2(UInt32 a, String b, DBusInterface c)
  {
   this.a = a;
   this.b = b;
   this.c = c;
  }
}
