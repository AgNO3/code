package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.UInt16;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct1 extends Struct
{
   @Position(0)
   public final byte a;
   @Position(1)
   public final String b;
   @Position(2)
   public final UInt16 c;
   @Position(3)
   public final int d;
   @Position(4)
   public final int e;
   @Position(5)
   public final int f;
   @Position(6)
   public final long g;
   @Position(7)
   public final int h;
   @Position(8)
   public final Map<String,Variant> i;
  public Struct1(byte a, String b, UInt16 c, int d, int e, int f, long g, int h, Map<String,Variant> i)
  {
   this.a = a;
   this.b = b;
   this.c = c;
   this.d = d;
   this.e = e;
   this.f = f;
   this.g = g;
   this.h = h;
   this.i = i;
  }
}
