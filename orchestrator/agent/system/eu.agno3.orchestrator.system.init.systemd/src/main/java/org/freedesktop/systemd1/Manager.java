package org.freedesktop.systemd1;
import java.util.List;
import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
@org.freedesktop.dbus.DBusInterfaceName("org.freedesktop.systemd1.Manager")
public interface Manager extends DBusInterface
{
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class UnitNew extends DBusSignal
   {
      public final String id;
      public final DBusInterface unit;
      public UnitNew(String path, String id, DBusInterface unit) throws DBusException
      {
         super(path, id, unit);
         this.id = id;
         this.unit = unit;
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class UnitRemoved extends DBusSignal
   {
      public final String id;
      public final DBusInterface unit;
      public UnitRemoved(String path, String id, DBusInterface unit) throws DBusException
      {
         super(path, id, unit);
         this.id = id;
         this.unit = unit;
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class JobNew extends DBusSignal
   {
      public final UInt32 id;
      public final DBusInterface job;
      public final String unit;
      public JobNew(String path, UInt32 id, DBusInterface job, String unit) throws DBusException
      {
         super(path, id, job, unit);
         this.id = id;
         this.job = job;
         this.unit = unit;
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class JobRemoved extends DBusSignal
   {
      public final UInt32 id;
      public final DBusInterface job;
      public final String unit;
      public final String result;
      public JobRemoved(String path, UInt32 id, DBusInterface job, String unit, String result) throws DBusException
      {
         super(path, id, job, unit, result);
         this.id = id;
         this.job = job;
         this.unit = unit;
         this.result = result;
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class StartupFinished extends DBusSignal
   {
      public final UInt64 firmware;
      public final UInt64 loader;
      public final UInt64 kernel;
      public final UInt64 initrd;
      public final UInt64 userspace;
      public final UInt64 total;
      public StartupFinished(String path, UInt64 firmware, UInt64 loader, UInt64 kernel, UInt64 initrd, UInt64 userspace, UInt64 total) throws DBusException
      {
         super(path, firmware, loader, kernel, initrd, userspace, total);
         this.firmware = firmware;
         this.loader = loader;
         this.kernel = kernel;
         this.initrd = initrd;
         this.userspace = userspace;
         this.total = total;
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class UnitFilesChanged extends DBusSignal
   {
      public UnitFilesChanged(String path) throws DBusException
      {
         super(path);
      }
   }
@javax.annotation.Generated("org.freedesktop.dbus.bin.createint.CreateInterface")
@java.lang.SuppressWarnings("all")
   public static class Reloading extends DBusSignal
   {
      public final boolean active;
      public Reloading(String path, boolean active) throws DBusException
      {
         super(path, active);
         this.active = active;
      }
   }

  public DBusInterface GetUnit(String name);
  public DBusInterface GetUnitByPID(UInt32 pid);
  public DBusInterface LoadUnit(String name);
  public DBusInterface StartUnit(String name, String mode);
  public DBusInterface StartUnitReplace(String old_unit, String new_unit, String mode);
  public DBusInterface StopUnit(String name, String mode);
  public DBusInterface ReloadUnit(String name, String mode);
  public DBusInterface RestartUnit(String name, String mode);
  public DBusInterface TryRestartUnit(String name, String mode);
  public DBusInterface ReloadOrRestartUnit(String name, String mode);
  public DBusInterface ReloadOrTryRestartUnit(String name, String mode);
  public void KillUnit(String name, String who, int signal);
  public void ResetFailedUnit(String name);
  public DBusInterface GetJob(UInt32 id);
  public void CancelJob(UInt32 id);
  public void ClearJobs();
  public void ResetFailed();
  public List<Struct1> ListUnits();
  public List<Struct2> ListJobs();
  public void Subscribe();
  public void Unsubscribe();
  public String Dump();
  public DBusInterface CreateSnapshot(String name, boolean cleanup);
  public void RemoveSnapshot(String name);
  public void Reload();
  public void Reexecute();
  public void Exit();
  public void Reboot();
  public void PowerOff();
  public void Halt();
  public void KExec();
  public void SwitchRoot(String new_root, String init);
  public void SetEnvironment(List<String> names);
  public void UnsetEnvironment(List<String> names);
  public void UnsetAndSetEnvironment(List<String> unset, List<String> set);
  public List<Struct3> ListUnitFiles();
  public String GetUnitFileState(String file);
  public Pair<Boolean, List<Struct4>> EnableUnitFiles(List<String> files, boolean runtime, boolean force);
  public List<Struct5> DisableUnitFiles(List<String> files, boolean runtime);
  public Pair<Boolean, List<Struct6>> ReenableUnitFiles(List<String> files, boolean runtime, boolean force);
  public List<Struct7> LinkUnitFiles(List<String> files, boolean runtime, boolean force);
  public Pair<Boolean, List<Struct8>> PresetUnitFiles(List<String> files, boolean runtime, boolean force);
  public List<Struct9> MaskUnitFiles(List<String> files, boolean runtime, boolean force);
  public List<Struct10> UnmaskUnitFiles(List<String> files, boolean runtime);
  public List<Struct11> SetDefaultTarget(List<String> files);
  public String GetDefaultTarget();
  public void SetUnitProperties(String name, boolean runtime, List<Struct12> properties);
  public DBusInterface StartTransientUnit(String name, String mode, List<Struct13> properties, List<Struct14> aux);

}
