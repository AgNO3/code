/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.principal.factors;


import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.AuthFactorType;


/**
 * @author mbechler
 *
 */
public class OneTimeFactor implements AuthFactor {

    /**
     * 
     */
    private static final long serialVersionUID = 1599646855838399430L;

    private final OneTimeType type;
    private final Boolean hardware;


    /**
     * 
     * @param type
     * @param hardware
     */
    public OneTimeFactor ( OneTimeType type, Boolean hardware ) {
        this.type = type;
        this.hardware = hardware;

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.AuthFactor#getFactorType()
     */
    @Override
    public AuthFactorType getFactorType () {
        return AuthFactorType.OTP;
    }


    /**
     * @return the type
     */
    public OneTimeType getType () {
        return this.type;
    }


    /**
     * @return the hardware
     */
    public Boolean isHardware () {
        return this.hardware;
    }
}
