package org.freedesktop.systemd1;


import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Struct;
import org.freedesktop.dbus.types.UInt32;


@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
public final class Struct1 extends Struct {

    @Position ( 0 )
    public final String a;
    @Position ( 1 )
    public final String b;
    @Position ( 2 )
    public final String c;
    @Position ( 3 )
    public final String d;
    @Position ( 4 )
    public final String e;
    @Position ( 5 )
    public final String f;
    @Position ( 6 )
    public final DBusInterface g;
    @Position ( 7 )
    public final UInt32 h;
    @Position ( 8 )
    public final String i;
    @Position ( 9 )
    public final DBusInterface j;


    public Struct1 ( String a, String b, String c, String d, String e, String f, DBusInterface g, UInt32 h, String i, DBusInterface j ) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
        this.h = h;
        this.i = i;
        this.j = j;
    }
}
