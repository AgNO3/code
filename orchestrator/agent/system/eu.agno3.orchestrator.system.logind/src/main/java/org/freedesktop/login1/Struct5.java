package org.freedesktop.login1;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.UInt32;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct5 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final String b;
   @Position(2)
   public final String c;
   @Position(3)
   public final String d;
   @Position(4)
   public final UInt32 e;
   @Position(5)
   public final UInt32 f;
  public Struct5(String a, String b, String c, String d, UInt32 e, UInt32 f)
  {
   this.a = a;
   this.b = b;
   this.c = c;
   this.d = d;
   this.e = e;
   this.f = f;
  }
}
