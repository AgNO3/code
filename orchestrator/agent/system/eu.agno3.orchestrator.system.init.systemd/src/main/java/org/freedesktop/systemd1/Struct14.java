package org.freedesktop.systemd1;
import java.util.List;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
public final class Struct14 extends Struct
{
   @Position(0)
   public final String a;
   @Position(1)
   public final List<Struct15> b;
  public Struct14(String a, List<Struct15> b)
  {
   this.a = a;
   this.b = b;
  }
}
