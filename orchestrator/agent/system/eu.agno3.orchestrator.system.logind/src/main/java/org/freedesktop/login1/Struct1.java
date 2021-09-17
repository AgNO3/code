package org.freedesktop.login1;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.UInt32;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct1 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final UInt32 b;
   @Position(2)
   public final String c;
   @Position(3)
   public final String d;
   @Position(4)
   public final DBusInterface e;
  public Struct1(String a, UInt32 b, String c, String d, DBusInterface e)
  {
   this.a = a;
   this.b = b;
   this.c = c;
   this.d = d;
   this.e = e;
  }
}
