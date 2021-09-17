package org.freedesktop.UDisks2;


import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.types.Variant;


@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
@org.freedesktop.dbus.DBusInterfaceName ( "org.freedesktop.UDisks2.Drive" )
public interface Drive extends DBusInterface {

    public void Eject ( Map<String, Variant> options );


    public void SetConfiguration ( Map<String, Variant> value, Map<String, Variant> options );


    public void PowerOff ( Map<String, Variant> options );

    public interface Ata extends DBusInterface {

        public void SmartUpdate ( Map<String, Variant> options );


        public List<Struct1> SmartGetAttributes ( Map<String, Variant> options );


        public void SmartSelftestStart ( String type, Map<String, Variant> options );


        public void SmartSelftestAbort ( Map<String, Variant> options );


        public void SmartSetEnabled ( boolean value, Map<String, Variant> options );


        public byte PmGetState ( Map<String, Variant> options );


        public void PmStandby ( Map<String, Variant> options );


        public void PmWakeup ( Map<String, Variant> options );


        public void SecurityEraseUnit ( Map<String, Variant> options );

    }

}