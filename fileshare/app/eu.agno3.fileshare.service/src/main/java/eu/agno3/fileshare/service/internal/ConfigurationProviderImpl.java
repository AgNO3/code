/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.internal;


import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.fileshare.service.ConfigurationProvider;
import eu.agno3.fileshare.service.config.FrontendConfiguration;
import eu.agno3.fileshare.service.config.MimeTypePolicyConfiguration;
import eu.agno3.fileshare.service.config.NotificationConfiguration;
import eu.agno3.fileshare.service.config.QuotaConfiguration;
import eu.agno3.fileshare.service.config.SearchConfiguration;
import eu.agno3.fileshare.service.config.SecurityPolicyConfiguration;
import eu.agno3.fileshare.service.config.TrustLevelConfiguration;
import eu.agno3.fileshare.service.config.UserConfiguration;
import eu.agno3.fileshare.service.config.ViewPolicyConfiguration;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigurationProvider.class )
public class ConfigurationProviderImpl implements ConfigurationProvider {

    private ViewPolicyConfiguration viewPolicyConfiguration;
    private MimeTypePolicyConfiguration mimeTypePolicyConfiguration;

    private UserConfiguration userConfig;
    private SecurityPolicyConfiguration securityPolicyConfiguration;
    private NotificationConfiguration notificationConfiguration;
    private FrontendConfiguration frontendConfiguration;
    private QuotaConfiguration quotaConfiguration;
    private TrustLevelConfiguration trustLevelConfiguration;
    private SearchConfiguration searchConfiguration;


    @Reference
    protected synchronized void setViewPolicyConfiguration ( ViewPolicyConfiguration vpc ) {
        this.viewPolicyConfiguration = vpc;
    }


    protected synchronized void unsetViewPolicyConfiguration ( ViewPolicyConfiguration vpc ) {
        if ( this.viewPolicyConfiguration == vpc ) {
            this.viewPolicyConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setMimeTypePolicyConfiguration ( MimeTypePolicyConfiguration mtpc ) {
        this.mimeTypePolicyConfiguration = mtpc;
    }


    protected synchronized void unsetMimeTypePolicyConfiguration ( MimeTypePolicyConfiguration mtpc ) {
        if ( this.mimeTypePolicyConfiguration == mtpc ) {
            this.mimeTypePolicyConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setUserConfiguration ( UserConfiguration uc ) {
        this.userConfig = uc;
    }


    protected synchronized void unsetUserConfiguration ( UserConfiguration uc ) {
        if ( this.userConfig == uc ) {
            this.userConfig = null;
        }
    }


    @Reference
    protected synchronized void setSecurityPolicyConfiguration ( SecurityPolicyConfiguration spc ) {
        this.securityPolicyConfiguration = spc;
    }


    protected synchronized void unsetSecurityPolicyConfiguration ( SecurityPolicyConfiguration spc ) {
        if ( this.securityPolicyConfiguration == spc ) {
            this.securityPolicyConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setNotificationConfiguration ( NotificationConfiguration nc ) {
        this.notificationConfiguration = nc;
    }


    protected synchronized void unsetNotificationConfiguration ( NotificationConfiguration nc ) {
        if ( this.notificationConfiguration == nc ) {
            this.notificationConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setFrontendConfiguration ( FrontendConfiguration fc ) {
        this.frontendConfiguration = fc;
    }


    protected synchronized void unsetFrontendConfiguration ( FrontendConfiguration fc ) {
        if ( this.frontendConfiguration == fc ) {
            this.frontendConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setQuotaConfiguration ( QuotaConfiguration qc ) {
        this.quotaConfiguration = qc;
    }


    protected synchronized void unsetQuotaConfiguration ( QuotaConfiguration qc ) {
        if ( this.quotaConfiguration == qc ) {
            this.quotaConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setTrustLevelConfiguration ( TrustLevelConfiguration tlc ) {
        this.trustLevelConfiguration = tlc;
    }


    protected synchronized void unsetTrustLevelConfiguration ( TrustLevelConfiguration tlc ) {
        if ( this.trustLevelConfiguration == tlc ) {
            this.trustLevelConfiguration = null;
        }
    }


    @Reference
    protected synchronized void setSearchConfiguration ( SearchConfiguration sc ) {
        this.searchConfiguration = sc;
    }


    protected synchronized void unsetSearchConfiguration ( SearchConfiguration sc ) {
        if ( this.searchConfiguration == sc ) {
            this.searchConfiguration = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getViewPolicyConfig()
     */
    @Override
    public ViewPolicyConfiguration getViewPolicyConfig () {
        return this.viewPolicyConfiguration;
    }


    /**
     * @return the mimeTypePolicyConfiguration
     */
    @Override
    public MimeTypePolicyConfiguration getMimeTypePolicyConfiguration () {
        return this.mimeTypePolicyConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getUserConfig()
     */
    @Override
    public UserConfiguration getUserConfig () {
        return this.userConfig;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getSecurityPolicyConfiguration()
     */
    @Override
    public SecurityPolicyConfiguration getSecurityPolicyConfiguration () {
        return this.securityPolicyConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getNotificationConfiguration()
     */
    @Override
    public NotificationConfiguration getNotificationConfiguration () {
        return this.notificationConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getFrontendConfiguration()
     */
    @Override
    public FrontendConfiguration getFrontendConfiguration () {
        return this.frontendConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getQuotaConfiguration()
     */
    @Override
    public QuotaConfiguration getQuotaConfiguration () {
        return this.quotaConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getTrustLevelConfiguration()
     */
    @Override
    public TrustLevelConfiguration getTrustLevelConfiguration () {
        return this.trustLevelConfiguration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.ConfigurationProvider#getSearchConfiguration()
     */
    @Override
    public SearchConfiguration getSearchConfiguration () {
        return this.searchConfiguration;
    }
}
