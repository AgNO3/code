/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import java.util.Locale;
import java.util.ResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.orchestrator.config.auth.RoleConfig;
import eu.agno3.runtime.i18n.StaticMapResourceBundleControl;
import eu.agno3.runtime.security.RoleTranslator;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FileshareAdminRoleTranslator implements RoleTranslator {

    @Inject
    private FileshareAdminConfigProviderImpl configProvider;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.RoleTranslator#getRoleTitle(java.lang.String, java.util.Locale)
     */
    @Override
    public String getRoleTitle ( String role, Locale l ) {
        RoleConfig roleConfig = getRoleConfig(role);
        if ( roleConfig == null ) {
            return role;
        }

        StaticMapResourceBundleControl ctl = new StaticMapResourceBundleControl(roleConfig.getTitles());
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, ctl).getString("msg"); //$NON-NLS-1$
    }


    /**
     * @param role
     * @return
     */
    private RoleConfig getRoleConfig ( String role ) {
        FileshareConfiguration config = this.configProvider.getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return null;
        }
        for ( RoleConfig rc : config.getAuthConfiguration().getRoleConfig().getRoles() ) {
            if ( role.equals(rc.getRoleId()) ) {
                return rc;
            }
        }
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.RoleTranslator#getRoleDescription(java.lang.String, java.util.Locale)
     */
    @Override
    public String getRoleDescription ( String role, Locale l ) {
        RoleConfig roleConfig = getRoleConfig(role);
        if ( roleConfig == null ) {
            return role;
        }

        StaticMapResourceBundleControl ctl = new StaticMapResourceBundleControl(roleConfig.getDescriptions());
        return ResourceBundle.getBundle(StringUtils.EMPTY, l, ctl).getString("msg"); //$NON-NLS-1$
    }

}
