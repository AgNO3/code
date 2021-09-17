package org.freedesktop.login1;


import java.util.List;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UnixFD;


@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
@org.freedesktop.dbus.DBusInterfaceName ( "org.freedesktop.login1.Manager" )
public interface Manager extends DBusInterface {

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class SessionNew extends DBusSignal {

        public final String id;
        public final DBusInterface path;


        public SessionNew ( String p, String id, DBusInterface path ) throws DBusException {
            super(p, id, path);
            this.id = id;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class SessionRemoved extends DBusSignal {

        public final String id;
        public final DBusInterface path;


        public SessionRemoved ( String p, String id, DBusInterface path ) throws DBusException {
            super(p, id, path);
            this.id = id;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class UserNew extends DBusSignal {

        public final UInt32 uid;
        public final DBusInterface path;


        public UserNew ( String p, UInt32 uid, DBusInterface path ) throws DBusException {
            super(p, uid, path);
            this.uid = uid;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class UserRemoved extends DBusSignal {

        public final UInt32 uid;
        public final DBusInterface path;


        public UserRemoved ( String p, UInt32 uid, DBusInterface path ) throws DBusException {
            super(p, uid, path);
            this.uid = uid;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class SeatNew extends DBusSignal {

        public final String id;
        public final DBusInterface path;


        public SeatNew ( String p, String id, DBusInterface path ) throws DBusException {
            super(p, id, path);
            this.id = id;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class SeatRemoved extends DBusSignal {

        public final String id;
        public final DBusInterface path;


        public SeatRemoved ( String p, String id, DBusInterface path ) throws DBusException {
            super(p, id, path);
            this.id = id;
            this.path = path;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class PrepareForShutdown extends DBusSignal {

        public final boolean active;


        public PrepareForShutdown ( String path, boolean active ) throws DBusException {
            super(path, active);
            this.active = active;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class PrepareForSleep extends DBusSignal {

        public final boolean active;


        public PrepareForSleep ( String path, boolean active ) throws DBusException {
            super(path, active);
            this.active = active;
        }
    }


    public DBusInterface GetSession ( String id );


    public DBusInterface GetSessionByPID ( UInt32 pid );


    public DBusInterface GetUser ( UInt32 uid );


    public DBusInterface GetUserByPID ( UInt32 pid );


    public DBusInterface GetSeat ( String id );


    public List<Struct1> ListSessions ();


    public List<Struct2> ListUsers ();


    public List<Struct3> ListSeats ();


    public Septuple<String, DBusInterface, DBusInterface, UnixFD, String, UInt32, Boolean> CreateSession ( UInt32 uid, UInt32 leader, String service,
            String type, String _class, String seat, UInt32 vtnr, String tty, String display, boolean remote, String remote_user, String remote_host,
            List<Struct4> scope_properties );


    public void ReleaseSession ( String id );


    public void ActivateSession ( String id );


    public void ActivateSessionOnSeat ( String id, String seat );


    public void LockSession ( String id );


    public void UnlockSession ( String id );


    public void LockSessions ();


    public void UnlockSessions ();


    public void KillSession ( String id, String who, String signal );


    public void KillUser ( UInt32 uid, String signal );


    public void TerminateSession ( String id );


    public void TerminateUser ( UInt32 uid );


    public void TerminateSeat ( String id );


    public void SetUserLinger ( UInt32 uid, boolean b, boolean interactive );


    public void AttachDevice ( String seat, String sysfs, boolean interactive );


    public void FlushDevices ( boolean interactive );


    public void PowerOff ( boolean interactive );


    public void Reboot ( boolean interactive );


    public void Suspend ( boolean interactive );


    public void Hibernate ( boolean interactive );


    public void HybridSleep ( boolean interactive );


    public String CanPowerOff ();


    public String CanReboot ();


    public String CanSuspend ();


    public String CanHibernate ();


    public String CanHybridSleep ();


    public UnixFD Inhibit ( String what, String who, String why, String mode );


    public List<Struct5> ListInhibitors ();

}
