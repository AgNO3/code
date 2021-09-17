/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.04.2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.webdav.security.Privilege;
import org.apache.jackrabbit.webdav.security.SupportedPrivilege;
import org.apache.shiro.SecurityUtils;

import eu.agno3.fileshare.model.GrantPermission;
import eu.agno3.fileshare.model.VFSEntity;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.runtime.security.principal.UserPrincipal;


/**
 * @author mbechler
 *
 */
public class DAVPrivileges {

    private static final String DEFAULT_LANG = "en"; //$NON-NLS-1$

    /**
     * 
     */
    public static Privilege OWNER_PRIVILEGE = Privilege.getPrivilege("owner", Constants.AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static Privilege UPLOAD_ONLY_PRIVILEGE = Privilege.getPrivilege("upload-only", Constants.AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static Privilege READ_PRIVILEGE = Privilege.getPrivilege("read", Constants.AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static Privilege UPLOAD_PRIVILEGE = Privilege.getPrivilege("upload", Constants.AGNO3_NS); //$NON-NLS-1$

    /**
     * 
     */
    public static Privilege EDIT_PRIVILEGE = Privilege.getPrivilege("edit", Constants.AGNO3_NS); //$NON-NLS-1$

    private Map<Locale, SupportedPrivilege> supportedContainerPrivilegeCache = new HashMap<>();
    private Map<Locale, SupportedPrivilege> supportedFilePrivilegeCache = new HashMap<>();
    private Map<Locale, SupportedPrivilege> supportedReadOnlyPrivilegeCache = new HashMap<>();

    private static Set<Privilege> ABSTRACT = new HashSet<>();
    private static Set<Privilege> SUPPORTED = new HashSet<>();


    static {
        ABSTRACT.add(Privilege.PRIVILEGE_ALL);
        ABSTRACT.add(Privilege.PRIVILEGE_BIND);
        ABSTRACT.add(Privilege.PRIVILEGE_READ);
        ABSTRACT.add(Privilege.PRIVILEGE_READ_ACL);
        ABSTRACT.add(Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET);
        ABSTRACT.add(Privilege.PRIVILEGE_UNBIND);
        ABSTRACT.add(Privilege.PRIVILEGE_UNLOCK);
        ABSTRACT.add(Privilege.PRIVILEGE_WRITE);
        ABSTRACT.add(Privilege.PRIVILEGE_WRITE_ACL);
        ABSTRACT.add(Privilege.PRIVILEGE_WRITE_CONTENT);
        ABSTRACT.add(Privilege.PRIVILEGE_WRITE_PROPERTIES);
        ABSTRACT.add(OWNER_PRIVILEGE);

        SUPPORTED.add(UPLOAD_ONLY_PRIVILEGE);
        SUPPORTED.add(READ_PRIVILEGE);
        SUPPORTED.add(UPLOAD_PRIVILEGE);
        SUPPORTED.add(EDIT_PRIVILEGE);
    }


    private static ResourceBundle getBundleFor ( Locale l ) {
        return ResourceBundle.getBundle("eu.agno3.fileshare.webdav.messages", l); //$NON-NLS-1$
    }


    /**
     * @param e
     * @param l
     * @return supported privilege attribute
     */
    public SupportedPrivilege getSupportedPrivilege ( VFSEntity e, Locale l ) {
        if ( e.isStaticReadOnly() ) {
            // no parents can be shared, cannot have inherited privileges
            return getSupportedReadOnlyPrivilege(l);
        }

        // we cannot just return the allowed privileges, inherited entries can refer to the disallowed ones
        // mark those unavailable abstract instead
        return getSupportedPrivileges(l, e instanceof VFSFileEntity);
    }


    /**
     * @param l
     * @return
     */
    private SupportedPrivilege getSupportedReadOnlyPrivilege ( Locale l ) {
        Map<Locale, SupportedPrivilege> cache = this.supportedReadOnlyPrivilegeCache;
        SupportedPrivilege cached = cache.get(l);
        if ( cached != null ) {
            return cached;
        }

        ResourceBundle b = getBundleFor(l);
        String langTag = b.getLocale().getLanguage();
        if ( StringUtils.isBlank(langTag) ) {
            langTag = DEFAULT_LANG;
        }

        final SupportedPrivilege davRead = new SupportedPrivilege(Privilege.PRIVILEGE_READ, b.getString("privilege.dav.read"), langTag, true, null); //$NON-NLS-1$
        final SupportedPrivilege davReadPrivileges = new SupportedPrivilege(
            Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET,
            b.getString("privilege.dav.read-current-user-privilege-set"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davListAcl = new SupportedPrivilege(
            Privilege.PRIVILEGE_READ_ACL,
            b.getString("privilege.dav.read-acl"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davWriteAcl = new SupportedPrivilege(Privilege.PRIVILEGE_WRITE_ACL, "privilege.dav.write-acl", langTag, true, null); //$NON-NLS-1$

        final SupportedPrivilege read = new SupportedPrivilege(
            READ_PRIVILEGE,
            b.getString("privilege.readOnly"), //$NON-NLS-1$
            langTag,
            false,
            new SupportedPrivilege[] {
                davRead, davReadPrivileges
        });

        final SupportedPrivilege owner = new SupportedPrivilege(
            OWNER_PRIVILEGE,
            b.getString("privilege.owner"), //$NON-NLS-1$
            langTag,
            true,
            new SupportedPrivilege[] {
                read, davListAcl, davWriteAcl
        });

        cached = owner;
        cache.put(l, cached);
        return cached;
    }


    /**
     * @param l
     * @param isFile
     * @return
     */
    private SupportedPrivilege getSupportedPrivileges ( Locale l, boolean isFile ) {
        Map<Locale, SupportedPrivilege> cache = isFile ? this.supportedFilePrivilegeCache : this.supportedContainerPrivilegeCache;
        SupportedPrivilege cached = cache.get(l);
        if ( cached != null ) {
            return cached;
        }

        ResourceBundle b = getBundleFor(l);
        String langTag = b.getLocale().getLanguage();
        if ( StringUtils.isBlank(langTag) ) {
            langTag = DEFAULT_LANG;
        }

        final SupportedPrivilege davRead = new SupportedPrivilege(Privilege.PRIVILEGE_READ, b.getString("privilege.dav.read"), langTag, true, null); //$NON-NLS-1$
        final SupportedPrivilege davWrite = new SupportedPrivilege(
            Privilege.PRIVILEGE_WRITE_CONTENT,
            b.getString("privilege.dav.write-content"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davBind = new SupportedPrivilege(Privilege.PRIVILEGE_BIND, b.getString("privilege.dav.bind"), langTag, true, null); //$NON-NLS-1$
        final SupportedPrivilege davUnbind = new SupportedPrivilege(
            Privilege.PRIVILEGE_UNBIND,
            b.getString("privilege.dav.unbind"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davReadPrivileges = new SupportedPrivilege(
            Privilege.PRIVILEGE_READ_CURRENT_USER_PRIVILEGE_SET,
            b.getString("privilege.dav.read-current-user-privilege-set"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davListAcl = new SupportedPrivilege(
            Privilege.PRIVILEGE_READ_ACL,
            b.getString("privilege.dav.read-acl"), //$NON-NLS-1$
            langTag,
            true,
            null);
        final SupportedPrivilege davWriteAcl = new SupportedPrivilege(Privilege.PRIVILEGE_WRITE_ACL, "privilege.dav.write-acl", langTag, true, null); //$NON-NLS-1$
        final SupportedPrivilege davModProperties = new SupportedPrivilege(
            Privilege.PRIVILEGE_WRITE_PROPERTIES,
            b.getString("privilege.dav.write-properties"), //$NON-NLS-1$
            langTag,
            true,
            null);

        final SupportedPrivilege uploadOnly = new SupportedPrivilege(
            UPLOAD_ONLY_PRIVILEGE,
            b.getString("privilege.uploadOnly"), //$NON-NLS-1$
            langTag,
            isFile,
            new SupportedPrivilege[] {
                davBind, davReadPrivileges
        });

        final SupportedPrivilege read = new SupportedPrivilege(
            READ_PRIVILEGE,
            b.getString("privilege.readOnly"), //$NON-NLS-1$
            langTag,
            false,
            new SupportedPrivilege[] {
                davRead, davReadPrivileges
        });

        final SupportedPrivilege upload = new SupportedPrivilege(
            UPLOAD_PRIVILEGE,
            b.getString("privilege.upload"), //$NON-NLS-1$
            langTag,
            isFile,
            new SupportedPrivilege[] {
                uploadOnly, read
        });

        final SupportedPrivilege edit = new SupportedPrivilege(
            EDIT_PRIVILEGE,
            b.getString("privilege.edit"), //$NON-NLS-1$
            langTag,
            false,
            new SupportedPrivilege[] {
                upload, davWrite, davUnbind
        });

        final SupportedPrivilege owner = new SupportedPrivilege(
            OWNER_PRIVILEGE,
            b.getString("privilege.owner"), //$NON-NLS-1$
            langTag,
            true,
            new SupportedPrivilege[] {
                edit, davListAcl, davWriteAcl, davModProperties
        });

        cached = owner;
        cache.put(l, cached);
        return cached;
    }


    /**
     * @param permissions
     * @param owner
     *            whether the current user is the owner of the target entity
     * @return privileges for these permissions
     */
    public Privilege[] mapPermissions ( Set<GrantPermission> permissions, boolean owner ) {
        if ( owner ) {
            return new Privilege[] {
                OWNER_PRIVILEGE
            };
        }

        if ( permissions.contains(GrantPermission.EDIT) ) {
            return new Privilege[] {
                EDIT_PRIVILEGE
            };
        }

        if ( permissions.contains(GrantPermission.UPLOAD) && !permissions.contains(GrantPermission.BROWSE) ) {
            return new Privilege[] {
                UPLOAD_ONLY_PRIVILEGE
            };
        }

        if ( permissions.contains(GrantPermission.UPLOAD) ) {
            return new Privilege[] {
                UPLOAD_PRIVILEGE
            };
        }

        if ( permissions.contains(GrantPermission.READ) ) {
            return new Privilege[] {
                READ_PRIVILEGE
            };
        }

        return new Privilege[0];
    }


    /**
     * @param validPrivileges
     * @return the grant permissions reflecting the given privileges
     */
    public Set<GrantPermission> mapToPermissions ( Set<Privilege> validPrivileges ) {
        Set<GrantPermission> perms = new HashSet<>();
        for ( Privilege priv : validPrivileges ) {
            if ( priv.equals(EDIT_PRIVILEGE) ) {
                perms.addAll(EnumSet.of(GrantPermission.EDIT, GrantPermission.READ, GrantPermission.BROWSE));
            }
            else if ( priv.equals(UPLOAD_PRIVILEGE) ) {
                perms.addAll(EnumSet.of(GrantPermission.UPLOAD, GrantPermission.EDIT_SELF, GrantPermission.READ, GrantPermission.BROWSE));
            }
            else if ( priv.equals(READ_PRIVILEGE) ) {
                perms.addAll(EnumSet.of(GrantPermission.READ, GrantPermission.BROWSE));
            }
            else if ( priv.equals(UPLOAD_ONLY_PRIVILEGE) ) {
                perms.addAll(EnumSet.of(GrantPermission.UPLOAD));
            }
        }
        return perms;
    }


    /**
     * @param priv
     * @return whether the privilege is considered abstract
     */
    public boolean isAbstract ( Privilege priv ) {
        // urgs ... no equality on privilege
        for ( Privilege match : ABSTRACT ) {
            if ( !Objects.equals(priv.getNamespace(), match.getNamespace()) ) {
                continue;
            }
            if ( !Objects.equals(priv.getName(), match.getName()) ) {
                continue;
            }
            return true;
        }
        return false;
    }


    /**
     * @param priv
     * @return whether the privilege is supported (does not include abstract privileges)
     */
    public boolean isSupported ( Privilege priv ) {
        // urgs ... no equality on privilege
        for ( Privilege match : SUPPORTED ) {
            if ( !Objects.equals(priv.getNamespace(), match.getNamespace()) ) {
                continue;
            }
            if ( !Objects.equals(priv.getName(), match.getName()) ) {
                continue;
            }
            return true;
        }
        return false;
    }


    /**
     * @param en
     * @return effective user permissions on this entity
     */
    public Integer mapEffectivePermissions ( EntityDAVNode en ) {
        if ( en.getEntity() == null || en.getEntity().isStaticReadOnly() ) {
            return GrantPermission.BROWSE.getStableId();
        }
        if ( en.getGrantId() == null ) {
            return GrantPermission.toInt(EnumSet.allOf(GrantPermission.class));
        }
        Set<GrantPermission> permissions = en.getPermissions();
        if ( permissions.contains(GrantPermission.EDIT_SELF) ) {
            UserPrincipal up = SecurityUtils.getSubject().getPrincipals().oneByType(UserPrincipal.class);
            if ( en.getEntity().getCreator() != null && en.getEntity().getCreator().getPrincipal().equals(up) ) {
                permissions.add(GrantPermission.EDIT);
            }
        }
        return GrantPermission.toInt(permissions);
    }

}
