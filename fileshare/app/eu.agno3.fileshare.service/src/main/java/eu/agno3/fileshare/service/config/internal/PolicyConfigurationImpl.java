/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.02.2015 by mbechler
 */
package eu.agno3.fileshare.service.config.internal;


import java.util.Collections;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.joda.time.Duration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.fileshare.model.GrantType;
import eu.agno3.fileshare.service.config.PolicyConfiguration;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = PolicyConfiguration.class, configurationPid = "policy", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class PolicyConfigurationImpl implements PolicyConfiguration {

    private static final Logger log = Logger.getLogger(PolicyConfigurationImpl.class);
    private String label;
    private float sortOrder;
    private Set<GrantType> allowedShareTypes;
    private Duration defaultExpirationDuration;
    private Duration maximumExpirationDuration;
    private Duration maximumShareLifetime;
    private Duration defaultShareLifetime;
    private Duration afterShareGracePeriod;
    private boolean transportRequireEncryption;
    private boolean transportRequirePFS;
    private int transportMinHashBlockSize;
    private int transportMinKeySize;
    private Set<String> requireAnyRole;
    private Set<String> disallowRoles;

    private Duration maxPasswordAge;
    private boolean requireHardwareFactor;
    private boolean alwaysCheckPasswordPolicy;
    private int minPasswordEntropy;
    private int minAuthFactors;

    private int minTokenPasswordEntropy;
    private boolean requireTokenPassword;
    private boolean noUserTokenPasswords;
    private boolean disallowWebDAVAccess;


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.sortOrder = ConfigUtil.parseFloat(cfg, "sortOrder", 0.0f); //$NON-NLS-1$

        parseAllowedShareTypes(cfg);

        this.maximumExpirationDuration = ConfigUtil.parseDuration(cfg, "maximumExpiration", null); //$NON-NLS-1$
        this.defaultExpirationDuration = ConfigUtil.parseDuration(cfg, "defaultExpiration", null); //$NON-NLS-1$
        this.maximumShareLifetime = ConfigUtil.parseDuration(cfg, "maximumShareLifetime", null); //$NON-NLS-1$
        this.defaultShareLifetime = ConfigUtil.parseDuration(cfg, "defaultShareLifetime", null); //$NON-NLS-1$
        this.afterShareGracePeriod = ConfigUtil.parseDuration(cfg, "afterShareGraceTime", Duration.standardDays(5)); //$NON-NLS-1$

        this.transportRequireEncryption = ConfigUtil.parseBoolean(cfg, "transportRequireEncryption", true); //$NON-NLS-1$
        this.transportRequirePFS = ConfigUtil.parseBoolean(cfg, "transportRequirePFS", false); //$NON-NLS-1$
        this.transportMinKeySize = ConfigUtil.parseInt(cfg, "transportMinKeySize", 128); //$NON-NLS-1$
        this.transportMinHashBlockSize = ConfigUtil.parseInt(cfg, "transportMinHashBlockSize", 128); //$NON-NLS-1$

        this.requireAnyRole = ConfigUtil.parseStringSet(cfg, "requireAnyRole", null); //$NON-NLS-1$
        this.disallowRoles = ConfigUtil.parseStringSet(cfg, "disallowRoles", null); //$NON-NLS-1$

        this.minAuthFactors = ConfigUtil.parseInt(cfg, "minimumAuthFactors", 0); //$NON-NLS-1$
        this.requireHardwareFactor = ConfigUtil.parseBoolean(cfg, "requireHardwareFactor", false); //$NON-NLS-1$
        this.maxPasswordAge = ConfigUtil.parseDuration(cfg, "maximumPasswordAge", null); //$NON-NLS-1$
        this.minPasswordEntropy = ConfigUtil.parseInt(cfg, "minimumPasswordEntropy", 0); //$NON-NLS-1$
        this.alwaysCheckPasswordPolicy = ConfigUtil.parseBoolean(cfg, "alwaysCheckPasswordPolicy", false); //$NON-NLS-1$

        this.minTokenPasswordEntropy = ConfigUtil.parseInt(cfg, "minimumTokenPasswordEntropy", 48); //$NON-NLS-1$
        this.requireTokenPassword = ConfigUtil.parseBoolean(cfg, "requireTokenPassword", false); //$NON-NLS-1$
        this.noUserTokenPasswords = ConfigUtil.parseBoolean(cfg, "noUserTokenPasswords", false); //$NON-NLS-1$

        this.disallowWebDAVAccess = ConfigUtil.parseBoolean(cfg, "disallowWebDAVAccess", false); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> properties = ctx.getProperties();
        this.label = ConfigUtil.parseString(properties, "label", null); //$NON-NLS-1$
        if ( this.label == null ) {
            throw new ComponentException("label is required " + //$NON-NLS-1$
                    ctx.getProperties().get("instanceId")); //$NON-NLS-1$
        }

        parseConfig(properties);

    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        parseConfig(ctx.getProperties());
    }


    /**
     * @param properties
     */
    private void parseAllowedShareTypes ( Dictionary<String, Object> properties ) {
        Set<String> shareTypes = ConfigUtil.parseStringSet(properties, "allowedShareTypes", Collections.EMPTY_SET); //$NON-NLS-1$
        Set<GrantType> grantTypes = EnumSet.noneOf(GrantType.class);
        for ( String shareType : shareTypes ) {
            try {
                grantTypes.add(GrantType.valueOf(shareType));
            }
            catch ( IllegalArgumentException e ) {
                log.warn("Could not parse share type ", e); //$NON-NLS-1$
            }
        }
        this.allowedShareTypes = grantTypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getDefaultExpirationDuration()
     */
    @Override
    public Duration getDefaultExpirationDuration () {
        return this.defaultExpirationDuration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getMaximumExpirationDuration()
     */
    @Override
    public Duration getMaximumExpirationDuration () {
        return this.maximumExpirationDuration;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getDefaultShareLifetime()
     */
    @Override
    public Duration getDefaultShareLifetime () {
        return this.defaultShareLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getMaximumShareLifetime()
     */
    @Override
    public Duration getMaximumShareLifetime () {
        return this.maximumShareLifetime;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getAfterShareGracePeriod()
     */
    @Override
    public Duration getAfterShareGracePeriod () {
        return this.afterShareGracePeriod;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getLabel()
     */
    @Override
    public String getLabel () {
        return this.label;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getSortOrder()
     */
    @Override
    public float getSortOrder () {
        return this.sortOrder;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getAllowedShareTypes()
     */
    @Override
    public Set<GrantType> getAllowedShareTypes () {
        return this.allowedShareTypes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#isTransportRequireEncryption()
     */
    @Override
    public boolean isTransportRequireEncryption () {
        return this.transportRequireEncryption;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#isTransportRequirePFS()
     */
    @Override
    public boolean isTransportRequirePFS () {
        return this.transportRequirePFS;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getTransportMinHashBlockSize()
     */
    @Override
    public int getTransportMinHashBlockSize () {
        return this.transportMinHashBlockSize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getTransportMinKeySize()
     */
    @Override
    public int getTransportMinKeySize () {
        return this.transportMinKeySize;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getRequireAnyRole()
     */
    @Override
    public Set<String> getRequireAnyRole () {
        return this.requireAnyRole;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getDisallowRoles()
     */
    @Override
    public Set<String> getDisallowRoles () {
        return this.disallowRoles;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getMinAuthFactors()
     */
    @Override
    public int getMinAuthFactors () {
        return this.minAuthFactors;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getMinimumPasswordEntropy()
     */
    @Override
    public int getMinimumPasswordEntropy () {
        return this.minPasswordEntropy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#isAlwaysCheckPasswordPolicy()
     */
    @Override
    public boolean isAlwaysCheckPasswordPolicy () {
        return this.alwaysCheckPasswordPolicy;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#isRequireHardwareFactor()
     */
    @Override
    public boolean isRequireHardwareFactor () {
        return this.requireHardwareFactor;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#getMaxPasswordAge()
     */
    @Override
    public Duration getMaxPasswordAge () {
        return this.maxPasswordAge;
    }


    /**
     * @return the minTokenPasswordEntropy
     */
    @Override
    public int getMinTokenPasswordEntropy () {
        return this.minTokenPasswordEntropy;
    }


    /**
     * @return the requireTokenPassword
     */
    @Override
    public boolean isRequireTokenPassword () {
        return this.requireTokenPassword;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.config.PolicyConfiguration#isNoUserTokenPasswords()
     */
    @Override
    public boolean isNoUserTokenPasswords () {
        return this.noUserTokenPasswords;
    }


    /**
     * @return the disallowWebDAVAccess
     */
    @Override
    public boolean isDisallowWebDAVAccess () {
        return this.disallowWebDAVAccess;
    }
}
