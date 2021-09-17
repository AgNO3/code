/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.09.2015 by mbechler
 */
package eu.agno3.fileshare.orch.webgui.admin;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.joda.time.DateTime;
import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.fileshare.orch.common.config.FileshareConfiguration;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.principal.AuthFactor;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class FilesharePasswordPolicyChecker implements PasswordPolicyChecker {

    @Inject
    @OsgiService ( dynamic = true, timeout = 200 )
    private PasswordPolicyChecker passwordPolicy;

    @Inject
    private FileshareAdminConfigProviderImpl configProvider;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#checkPasswordValid(java.lang.String,
     *      org.joda.time.DateTime)
     */
    @Override
    public AuthFactor checkPasswordValid ( String password, DateTime lastPwChange ) {
        return this.passwordPolicy.checkPasswordValid(password, lastPwChange);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#checkPasswordChangeValid(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public AuthFactor checkPasswordChangeValid ( String password, String oldPassword ) {
        return this.passwordPolicy.checkPasswordChangeValid(oldPassword, oldPassword);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#estimateEntropy(java.lang.String)
     */
    @Override
    public int estimateEntropy ( String password ) {
        return this.passwordPolicy.estimateEntropy(password);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#getEntropyLowerLimit()
     */
    @Override
    public int getEntropyLowerLimit () {
        FileshareConfiguration config = this.configProvider.getEffectiveFileshareConfiguration();
        if ( config == null ) {
            return this.passwordPolicy.getEntropyLowerLimit();
        }
        return config.getAuthConfiguration().getAuthenticators().getPasswordPolicy().getEntropyLowerLimit();
    }
}
