package org.freedesktop.systemd1;


import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Tuple;


/** Just a typed container class */
@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
public final class Pair <A, B> extends Tuple {

    @Position ( 0 )
    public final A a;
    @Position ( 1 )
    public final B b;


    public Pair ( A a, B b ) {
        this.a = a;
        this.b = b;
    }
}
