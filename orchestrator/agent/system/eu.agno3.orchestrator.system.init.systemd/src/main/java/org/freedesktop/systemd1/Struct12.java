package org.freedesktop.systemd1;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.Variant;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct12 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final Variant b;
  public Struct12(String a, Variant b)
  {
   this.a = a;
   this.b = b;
  }
}
