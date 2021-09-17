package org.freedesktop.UDisks2;
import java.util.Map;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct2 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final Map<String,Variant> b;
  public Struct2(String a, Map<String,Variant> b)
  {
   this.a = a;
   this.b = b;
  }
}
