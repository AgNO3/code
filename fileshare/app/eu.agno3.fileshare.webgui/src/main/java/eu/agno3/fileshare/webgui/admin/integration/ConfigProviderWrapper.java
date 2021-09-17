/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2015 by mbechler
 */
package eu.agno3.fileshare.webgui.admin.integration;


import java.util.List;
import java.util.Set;

import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider;
import eu.agno3.fileshare.webgui.login.AuthInfoBean;


/**
 * @author mbechler
 *
 */
public class ConfigProviderWrapper implements FileshareAdminConfigProvider {

    private ConfigurationProvider configurationProvider;
    private AuthInfoBean authInfo;


    /**
     * @param configurationProvider
     * @param authInfo
     */
    public ConfigProviderWrapper ( ConfigurationProvider configurationProvider, AuthInfoBean authInfo ) {
        this.configurationProvider = configurationProvider;
        this.authInfo = authInfo;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getDefinedSecurityLabels()
     */
    @Override
    public List<String> getDefinedSecurityLabels () {
        return this.configurationProvider.getSecurityPolicyConfiguration().getDefinedLabels();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getGlobalDefaultQuota()
     */
    @Override
    public Long getGlobalDefaultQuota () {
        return this.configurationProvider.getQuotaConfiguration().getGlobalDefaultQuota();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#getUserDefaultRoles()
     */
    @Override
    public Set<String> getUserDefaultRoles () {
        return this.configurationProvider.getUserConfig().getDefaultRoles();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.webgui.admin.FileshareAdminConfigProvider#isMultiRealm()
     */
    @Override
    public boolean isMultiRealm () {
        return this.authInfo.getMultiRealm();
    }
}
