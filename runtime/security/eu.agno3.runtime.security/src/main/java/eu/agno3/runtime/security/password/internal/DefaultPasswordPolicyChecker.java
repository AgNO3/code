/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.password.internal;


import java.util.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.ReadableDuration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.security.password.PasswordChangePolicyException;
import eu.agno3.runtime.security.password.PasswordEntropyEstimator;
import eu.agno3.runtime.security.password.PasswordPolicyChecker;
import eu.agno3.runtime.security.password.PasswordPolicyException;
import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.factors.PasswordFactor;
import eu.agno3.runtime.util.config.ConfigUtil;


/**
 * @author mbechler
 *
 */
@Component ( service = PasswordPolicyChecker.class, configurationPid = "password.policy" )
public class DefaultPasswordPolicyChecker implements PasswordPolicyChecker {

    private PasswordEntropyEstimator estimator;
    private int entropyLowerLimit;
    private ReadableDuration maximumPasswordAge;
    private boolean ignoreUnknownAge;


    @Reference
    protected synchronized void setPasswordEntropyEstimator ( PasswordEntropyEstimator pee ) {
        this.estimator = pee;
    }


    protected synchronized void unsetPasswordEntropyEstimator ( PasswordEntropyEstimator pee ) {
        if ( this.estimator == pee ) {
            this.estimator = null;
        }
    }


    /**
     * @param cfg
     */
    private void parseConfig ( Dictionary<String, Object> cfg ) {
        this.entropyLowerLimit = ConfigUtil.parseInt(cfg, "minEntropy", 40); //$NON-NLS-1$
        this.maximumPasswordAge = ConfigUtil.parseDuration(cfg, "maxAge", null); //$NON-NLS-1$
        this.ignoreUnknownAge = ConfigUtil.parseBoolean(cfg, "ignoreUnknownAge", false); //$NON-NLS-1$
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        parseConfig(cfg);
    }


    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        Dictionary<String, Object> cfg = ctx.getProperties();
        parseConfig(cfg);
    }


    /**
     * @return the entropyLowerLimit
     */
    @Override
    public int getEntropyLowerLimit () {
        return this.entropyLowerLimit;
    }


    /**
     * 
     * @param password
     * @return the estimated entropy
     */
    @Override
    public int estimateEntropy ( String password ) {
        return this.estimator.estimateEntropy(password);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#checkPasswordChangeValid(java.lang.String,
     *      java.lang.String)
     */
    @Override
    public AuthFactor checkPasswordChangeValid ( String password, String oldPassword ) {
        if ( StringUtils.isBlank(password) ) {
            throw new PasswordChangePolicyException("Password is empty"); //$NON-NLS-1$
        }

        if ( password.equals(oldPassword) ) {
            throw new PasswordChangePolicyException("Old password equals new password"); //$NON-NLS-1$
        }

        int entropy = this.estimator.estimateEntropy(password);
        if ( entropy < this.entropyLowerLimit ) {
            throw new PasswordChangePolicyException(String.format("New password is too weak, estimated %d bit entropy", entropy)); //$NON-NLS-1$
        }

        return new PasswordFactor(entropy, Duration.millis(0));
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.password.PasswordPolicyChecker#checkPasswordValid(java.lang.String,
     *      org.joda.time.DateTime)
     */
    @Override
    public AuthFactor checkPasswordValid ( String password, DateTime lastPwChange ) {
        if ( StringUtils.isBlank(password) ) {
            throw new PasswordPolicyException("Password is empty"); //$NON-NLS-1$
        }
        int entropy = this.estimator.estimateEntropy(password);
        if ( entropy < this.entropyLowerLimit ) {
            throw new PasswordPolicyException(String.format("New password is too weak, estimated %d bit entropy", entropy)); //$NON-NLS-1$
        }

        // Could also add password age restriction on crack time estimation
        Duration age = null;
        if ( lastPwChange != null ) {
            age = new Duration(lastPwChange, DateTime.now());
            if ( this.maximumPasswordAge != null && age.isLongerThan(this.maximumPasswordAge) ) {
                throw new PasswordPolicyException("Password is too old"); //$NON-NLS-1$
            }
        }
        else if ( !this.ignoreUnknownAge && this.maximumPasswordAge != null ) {
            throw new PasswordPolicyException("Password age is unknown"); //$NON-NLS-1$
        }
        return new PasswordFactor(entropy, age);
    }
}
