/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.10.2014 by mbechler
 */
package eu.agno3.runtime.security.password;


import org.joda.time.DateTime;

import eu.agno3.runtime.security.principal.AuthFactor;


/**
 * @author mbechler
 *
 */
public interface PasswordPolicyChecker {

    /**
     * 
     * @param password
     * @param lastPwChange
     *            last password change if known
     * @param oldPassword
     *            old password, may be null
     * @return a password auth factor with entropy estimation
     * 
     * @throws PasswordPolicyException
     *             if the password is not acceptable
     */
    AuthFactor checkPasswordValid ( String password, DateTime lastPwChange );


    /**
     * 
     * @param password
     * @param oldPassword
     *            old password, may be null
     * @return a password auth factor with entropy estimation
     * 
     * @throws PasswordChangePolicyException
     *             if the password is not acceptable
     */
    AuthFactor checkPasswordChangeValid ( String password, String oldPassword );


    /**
     * @param password
     * @return estimated entropy
     */
    int estimateEntropy ( String password );


    /**
     * @return entropy estimate lower limit
     */
    int getEntropyLowerLimit ();
}
