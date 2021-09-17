package org.freedesktop.login1;


import org.freedesktop.dbus.Position;
import org.freedesktop.dbus.types.Tuple;


/** Just a typed container class */
@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
public final class Septuple <A, B, C, D, E, F, G> extends Tuple {

    @Position ( 0 )
    public final A a;
    @Position ( 1 )
    public final B b;
    @Position ( 2 )
    public final C c;
    @Position ( 3 )
    public final D d;
    @Position ( 4 )
    public final E e;
    @Position ( 5 )
    public final F f;
    @Position ( 6 )
    public final G g;


    public Septuple ( A a, B b, C c, D d, E e, F f, G g ) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
        this.g = g;
    }
}
