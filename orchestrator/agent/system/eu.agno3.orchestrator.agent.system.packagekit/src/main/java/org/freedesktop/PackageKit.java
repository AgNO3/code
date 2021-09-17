package org.freedesktop;


import java.util.List;
import java.util.Map;

import org.freedesktop.dbus.DBusInterface;
import org.freedesktop.dbus.DBusSignal;
import org.freedesktop.dbus.exceptions.DBusException;
import org.freedesktop.dbus.types.UInt32;
import org.freedesktop.dbus.types.UInt64;
import org.freedesktop.dbus.types.Variant;


@javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
@java.lang.SuppressWarnings ( "all" )
@org.freedesktop.dbus.DBusInterfaceName ( "org.freedesktop.PackageKit" )
public interface PackageKit extends DBusInterface {

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class TransactionListChanged extends DBusSignal {

        public final List<String> transactions;


        public TransactionListChanged ( String path, List<String> transactions ) throws DBusException {
            super(path, transactions);
            this.transactions = transactions;
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class RestartSchedule extends DBusSignal {

        public RestartSchedule ( String path ) throws DBusException {
            super(path);
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class RepoListChanged extends DBusSignal {

        public RepoListChanged ( String path ) throws DBusException {
            super(path);
        }
    }

    @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
    @java.lang.SuppressWarnings ( "all" )
    public static class UpdatesChanged extends DBusSignal {

        public UpdatesChanged ( String path ) throws DBusException {
            super(path);
        }
    }


    @Async ( )
    public UInt32 CanAuthorize ( String action_id );


    @Async ( )
    public DBusInterface CreateTransaction ();


    public UInt32 GetTimeSinceAction ( UInt32 role );


    public List<DBusInterface> GetTransactionList ();


    public void StateHasChanged ( String reason );


    public void SuggestDaemonQuit ();


    public Map<String, List<Map<String, Variant>>> GetPackageHistory ( List<String> names, UInt32 count );


    public String GetDaemonState ();


    @Async ( )
    public void SetProxy ( String proxy_http, String proxy_https, String proxy_ftp, String proxy_socks, String no_proxy, String pac );

    @org.freedesktop.dbus.DBusInterfaceName ( "org.freedesktop.PackageKit.Offline" )
    public interface Offline extends DBusInterface {

        public void ClearResults ();


        public void Trigger ( String action );


        public void Cancel ();


        public List<String> GetPrepared ();

    }

    @org.freedesktop.dbus.DBusInterfaceName ( "org.freedesktop.PackageKit.Transaction" )
    public interface Transaction extends DBusInterface {

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Category extends DBusSignal {

            public final String parent_id;
            public final String cat_id;
            public final String name;
            public final String summary;
            public final String icon;


            public Category ( String path, String parent_id, String cat_id, String name, String summary, String icon ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Category", null);
                this.parent_id = parent_id;
                this.cat_id = cat_id;
                this.name = name;
                this.summary = summary;
                this.icon = icon;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Details extends DBusSignal {

            public final Map<String, Variant> data;


            public Details ( String path, Map<String, Variant> data ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Details", null);
                this.data = data;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class ErrorCode extends DBusSignal {

            public final UInt32 code;
            public final String details;


            public ErrorCode ( String path, UInt32 code, String details ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "ErrorCode", null);
                this.code = code;
                this.details = details;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Files extends DBusSignal {

            public final String package_id;
            public final List<String> file_list;


            public Files ( String path, String package_id, List<String> file_list ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Files", null);
                this.package_id = package_id;
                this.file_list = file_list;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Finished extends DBusSignal {

            public final UInt32 exit;
            public final UInt32 runtime;


            public Finished ( String path, UInt32 exit, UInt32 runtime ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Finished", null);
                this.exit = exit;
                this.runtime = runtime;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Package extends DBusSignal {

            public final UInt32 info;
            public final String package_id;
            public final String summary;


            public Package ( String path, UInt32 info, String package_id, String summary ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Package", null);
                this.info = info;
                this.package_id = package_id;
                this.summary = summary;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class RepoDetail extends DBusSignal {

            public final String repo_id;
            public final String description;
            public final boolean enabled;


            public RepoDetail ( String path, String repo_id, String description, boolean enabled ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "RepoDetail", null);
                this.repo_id = repo_id;
                this.description = description;
                this.enabled = enabled;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class RepoSignatureRequired extends DBusSignal {

            public final String package_id;
            public final String repository_name;
            public final String key_url;
            public final String key_userid;
            public final String key_id;
            public final String key_fingerprint;
            public final String key_timestamp;
            public final UInt32 type;


            public RepoSignatureRequired ( String path, String package_id, String repository_name, String key_url, String key_userid, String key_id,
                    String key_fingerprint, String key_timestamp, UInt32 type ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "RepoSignatureRequired", null);
                this.package_id = package_id;
                this.repository_name = repository_name;
                this.key_url = key_url;
                this.key_userid = key_userid;
                this.key_id = key_id;
                this.key_fingerprint = key_fingerprint;
                this.key_timestamp = key_timestamp;
                this.type = type;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class EulaRequired extends DBusSignal {

            public final String eula_id;
            public final String package_id;
            public final String vendor_name;
            public final String license_agreement;


            public EulaRequired ( String path, String eula_id, String package_id, String vendor_name, String license_agreement )
                    throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "EulaRequired", null);
                this.eula_id = eula_id;
                this.package_id = package_id;
                this.vendor_name = vendor_name;
                this.license_agreement = license_agreement;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class MediaChangeRequired extends DBusSignal {

            public final UInt32 media_type;
            public final String media_id;
            public final String media_text;


            public MediaChangeRequired ( String path, UInt32 media_type, String media_id, String media_text ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "MediaChangeRequired", null);
                this.media_type = media_type;
                this.media_id = media_id;
                this.media_text = media_text;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class RequireRestart extends DBusSignal {

            public final UInt32 type;
            public final String package_id;


            public RequireRestart ( String path, UInt32 type, String package_id ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "RequireRestart", null);
                this.type = type;
                this.package_id = package_id;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class TransactionSignal extends DBusSignal {

            public final DBusInterface object_path;
            public final String timespec;
            public final boolean succeeded;
            public final UInt32 role;
            public final UInt32 duration;
            public final String data;
            public final UInt32 uid;
            public final String cmdline;


            public TransactionSignal ( String path, DBusInterface object_path, String timespec, boolean succeeded, UInt32 role, UInt32 duration,
                    String data, UInt32 uid, String cmdline ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Transaction", null);
                this.object_path = object_path;
                this.timespec = timespec;
                this.succeeded = succeeded;
                this.role = role;
                this.duration = duration;
                this.data = data;
                this.uid = uid;
                this.cmdline = cmdline;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class UpdateDetail extends DBusSignal {

            public final String package_id;
            public final List<String> updates;
            public final List<String> obsoletes;
            public final List<String> vendor_urls;
            public final List<String> bugzilla_urls;
            public final List<String> cve_urls;
            public final UInt32 restart;
            public final String update_text;
            public final String changelog;
            public final UInt32 state;
            public final String issued;
            public final String updated;


            public UpdateDetail ( String path, String package_id, List<String> updates, List<String> obsoletes, List<String> vendor_urls,
                    List<String> bugzilla_urls, List<String> cve_urls, UInt32 restart, String update_text, String changelog, UInt32 state,
                    String issued, String updated ) throws DBusException {

                super(null, path, "org.freedesktop.PackageKit.Transaction", "UpdateDetail", null);
                this.package_id = package_id;
                this.updates = updates;
                this.obsoletes = obsoletes;
                this.vendor_urls = vendor_urls;
                this.bugzilla_urls = bugzilla_urls;
                this.cve_urls = cve_urls;
                this.restart = restart;
                this.update_text = update_text;
                this.changelog = changelog;
                this.state = state;
                this.issued = issued;
                this.updated = updated;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class DistroUpgrade extends DBusSignal {

            public final UInt32 type;
            public final String name;
            public final String summary;


            public DistroUpgrade ( String path, UInt32 type, String name, String summary ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "DistroUpgrade", null);
                this.type = type;
                this.name = name;
                this.summary = summary;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class ItemProgress extends DBusSignal {

            public final String id;
            public final UInt32 status;
            public final UInt32 percentage;


            public ItemProgress ( String path, String id, UInt32 status, UInt32 percentage ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "ItemProgress", null);
                this.id = id;
                this.status = status;
                this.percentage = percentage;
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class Destroy extends DBusSignal {

            public Destroy ( String path ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "Destroy", null);
            }
        }

        @javax.annotation.Generated ( "org.freedesktop.dbus.bin.createint.CreateInterface" )
        @java.lang.SuppressWarnings ( "all" )
        public static class NotRunning extends DBusSignal {

            public NotRunning ( String path ) throws DBusException {
                super(null, path, "org.freedesktop.PackageKit.Transaction", "NotRunning", null);
            }
        }


        @Async ( )
        public void SetHints ( List<String> hints );


        @Async ( )
        public void AcceptEula ( String eula_id );


        @Async ( )
        public void Cancel ();


        @Async ( )
        public void DownloadPackages ( boolean store_in_cache, List<String> package_ids );


        @Async ( )
        public void GetCategories ();


        @Async ( )
        public void DependsOn ( UInt64 filter, List<String> package_ids, boolean recursive );


        @Async ( )
        public void GetDetails ( List<String> package_ids );


        @Async ( )
        public void GetDetailsLocal ( List<String> files );


        @Async ( )
        public void GetFilesLocal ( List<String> files );


        @Async ( )
        public void GetFiles ( List<String> package_ids );


        public void GetOldTransactions ( UInt32 number );


        @Async ( )
        public void GetPackages ( UInt64 filter );


        @Async ( )
        public void GetRepoList ( UInt64 filter );


        @Async ( )
        public void RequiredBy ( UInt64 filter, List<String> package_ids, boolean recursive );


        @Async ( )
        public void GetUpdateDetail ( List<String> package_ids );


        @Async ( )
        public void GetUpdates ( UInt64 filter );


        @Async ( )
        public void GetDistroUpgrades ();


        @Async ( )
        public void InstallFiles ( UInt64 transaction_flags, List<String> full_paths );


        @Async ( )
        public void InstallPackages ( UInt64 transaction_flags, List<String> package_ids );


        @Async ( )
        public void InstallSignature ( UInt32 sig_type, String key_id, String package_id );


        @Async ( )
        public void RefreshCache ( boolean force );


        @Async ( )
        public void RemovePackages ( UInt64 transaction_flags, List<String> package_ids, boolean allow_deps, boolean autoremove );


        @Async ( )
        public void RepoEnable ( String repo_id, boolean enabled );


        @Async ( )
        public void RepoSetData ( String repo_id, String parameter, String value );


        @Async ( )
        public void RepoRemove ( UInt64 transaction_flags, String repo_id, boolean autoremove );


        @Async ( )
        public void Resolve ( UInt64 filter, List<String> packages );


        @Async ( )
        public void SearchDetails ( UInt64 filter, List<String> values );


        @Async ( )
        public void SearchFiles ( UInt64 filter, List<String> values );


        @Async ( )
        public void SearchGroups ( UInt64 filter, List<String> values );


        @Async ( )
        public void SearchNames ( UInt64 filter, List<String> values );


        @Async ( )
        public void UpdatePackages ( UInt64 transaction_flags, List<String> package_ids );


        @Async ( )
        public void WhatProvides ( UInt64 filter, List<String> values );


        @Async ( )
        public void RepairSystem ( UInt64 transaction_flags );

    }

}
