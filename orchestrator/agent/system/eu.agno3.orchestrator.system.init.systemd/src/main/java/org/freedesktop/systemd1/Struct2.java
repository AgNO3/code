package org.freedesktop.systemd1;
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
   public final String c;
   @Position(3)
   public final String d;
   @Position(4)
   public final DBusInterface e;
   @Position(5)
   public final DBusInterface f;
  public Struct2(UInt32 a, String b, String c, String d, DBusInterface e, DBusInterface f)
  {
   this.a = a;
   this.b = b;
   this.c = c;
   this.d = d;
   this.e = e;
   this.f = f;
  }
}
