/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.principal.factors;


import org.joda.time.Duration;

import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.AuthFactorType;


/**
 * @author mbechler
 *
 */
public class PasswordFactor implements AuthFactor {

    /**
     * 
     */
    private static final long serialVersionUID = -7955679494598598140L;
    private Integer entropy;
    private Duration age;


    /**
     * @param entropy
     * @param age
     * 
     */
    public PasswordFactor ( Integer entropy, Duration age ) {
        this.entropy = entropy;
        this.age = age;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.AuthFactor#getFactorType()
     */
    @Override
    public AuthFactorType getFactorType () {
        return AuthFactorType.PASSWORD;
    }


    /**
     * @return the entropy
     */
    public Integer getEntropy () {
        return this.entropy;
    }


    /**
     * @return the age
     */
    public Duration getAge () {
        return this.age;
    }

}
