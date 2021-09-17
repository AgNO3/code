/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.01.2015 by mbechler
 */
package eu.agno3.runtime.security.internal;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.permission.AllPermission;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.i18n.StaticMapResourceBundleControl;
import eu.agno3.runtime.security.PermissionMapper;
import eu.agno3.runtime.security.RolePermissionContributor;
import eu.agno3.runtime.security.RoleTranslator;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = {
    RolePermissionContributor.class, PermissionMapper.class, RoleTranslator.class
}, configurationPid = "roles", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class StaticPermissionMapperImpl implements PermissionMapper, RolePermissionContributor, RoleTranslator {

    /**
     * 
     */
    private static final String ADMIN = "ADMIN"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(StaticPermissionMapperImpl.class);

    private Map<String, Set<Permission>> roleDefs = new HashMap<>();
    private Set<String> listRoles = new HashSet<>();

    private Map<String, Map<Locale, String>> titles = new HashMap<>();
    private Map<String, Map<Locale, String>> descriptions = new HashMap<>();
    private Map<String, Control> cachedTitleControls = new HashMap<>();
    private Map<String, Control> cachedDescriptionControls = new HashMap<>();


    @Activate
    @Modified
    protected synchronized void activate ( ComponentContext ctx ) {
        Set<String> roles = ConfigUtil.parseStringSet(ctx.getProperties(), "roles", null); //$NON-NLS-1$
        if ( roles == null ) {
            log.warn("No role definitions found, creating fallback ADMIN role"); //$NON-NLS-1$
            this.roleDefs.put(ADMIN, new HashSet<>(Arrays.asList(new AllPermission())));
            this.listRoles.add(ADMIN);
            return;
        }

        Map<String, Set<Permission>> newRoleMap = new HashMap<>();
        Set<String> newListRoles = new HashSet<>();

        for ( String role : roles ) {
            role = role.trim();
            Set<String> perms = ConfigUtil.parseStringSet(ctx.getProperties(), role + "_perms", null); //$NON-NLS-1$
            boolean list = ConfigUtil.parseBoolean(ctx.getProperties(), role + "_list", true); //$NON-NLS-1$

            if ( list ) {
                newListRoles.add(role);
            }

            if ( perms != null ) {
                newRoleMap.put(role, setupPermissions(role, perms));
            }
            setupLocalization(ctx.getProperties(), role);
        }

        this.cachedTitleControls.clear();
        this.cachedDescriptionControls.clear();
        this.roleDefs = newRoleMap;
        this.listRoles = newListRoles;
    }


    /**
     * @param properties
     * @param role
     */
    private void setupLocalization ( Dictionary<String, Object> properties, String role ) {
        Enumeration<String> keys = properties.keys();
        Map<Locale, String> ttls = new HashMap<>();
        Map<Locale, String> descs = new HashMap<>();
        if ( keys != null ) {
            while ( keys.hasMoreElements() ) {
                String key = keys.nextElement();
                if ( !key.startsWith(role) ) {
                    continue;
                }
                Object value = properties.get(key);
                if ( key.startsWith(role + "_title_") ) { //$NON-NLS-1$
                    ttls.put(Locale.forLanguageTag(key.substring(role.length() + 7)), (String) value);
                }
                else if ( key.startsWith(role + "_description_") ) { //$NON-NLS-1$
                    descs.put(Locale.forLanguageTag(key.substring(role.length() + 13)), (String) value);
                }
                else if ( key.startsWith(role + "_title") ) { //$NON-NLS-1$
                    ttls.put(Locale.ROOT, (String) value);
                }
                else if ( key.startsWith(role + "_description") ) { //$NON-NLS-1$
                    descs.put(Locale.ROOT, (String) value);
                }
            }
        }
        this.descriptions.put(role, descs);
        this.titles.put(role, ttls);

    }


    /**
     * @param newRoleMap
     * @param role
     * @param perms
     * @return
     */
    private static Set<Permission> setupPermissions ( String role, Set<String> perms ) {
        Set<Permission> permObjs = new HashSet<>();
        for ( String perm : perms ) {
            if ( "*".equals(perm.trim()) ) { //$NON-NLS-1$
                permObjs.add(new AllPermission());
            }
            else {
                permObjs.add(new WildcardPermission(perm.trim()));
            }
        }
        return permObjs;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.PermissionMapper#getPermissionsForRoles(java.util.Collection)
     */
    @Override
    public Set<Permission> getPermissionsForRoles ( Collection<String> roles ) {
        Set<Permission> res = new HashSet<>();
        for ( String role : roles ) {
            Set<Permission> perms = this.roleDefs.get(role);

            if ( perms != null ) {
                res.addAll(perms);
            }
        }

        return res;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.shiro.authz.permission.RolePermissionResolver#resolvePermissionsInRole(java.lang.String)
     */
    @Override
    public Collection<Permission> resolvePermissionsInRole ( String role ) {
        Set<Permission> perms = this.roleDefs.get(role);
        if ( perms == null ) {
            return Collections.EMPTY_SET;
        }
        return Collections.unmodifiableSet(perms);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.RoleTranslator#getRoleTitle(java.lang.String, java.util.Locale)
     */
    @Override
    public String getRoleTitle ( String role, Locale l ) {
        Control bundleControl = this.getTitleBundleControl(role);
        if ( bundleControl == null ) {
            return role;
        }
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, bundleControl).getString("msg"); //$NON-NLS-1$
    }


    /**
     * @param role
     * @return
     */
    private Control getTitleBundleControl ( String role ) {
        if ( !this.titles.containsKey(role) ) {
            return null;
        }
        Control c = this.cachedTitleControls.get(role);
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.titles.get(role));
            this.cachedTitleControls.put(role, c);
        }
        return c;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.RoleTranslator#getRoleDescription(java.lang.String, java.util.Locale)
     */
    @Override
    public String getRoleDescription ( String role, Locale l ) {
        Control bundleControl = this.getDescriptionBundleControl(role);
        if ( bundleControl == null ) {
            return role;
        }
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, bundleControl).getString("msg"); //$NON-NLS-1$
    }


    /**
     * @param role
     * @return
     */
    private Control getDescriptionBundleControl ( String role ) {
        if ( !this.descriptions.containsKey(role) ) {
            return null;
        }
        Control c = this.cachedDescriptionControls.get(role);
        if ( c == null ) {
            c = new StaticMapResourceBundleControl(this.descriptions.get(role));
            this.cachedDescriptionControls.put(role, c);
        }
        return c;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.PermissionMapper#getDefinedRoles()
     */
    @Override
    public Collection<String> getDefinedRoles () {
        return Collections.unmodifiableSet(this.listRoles);
    }
}
