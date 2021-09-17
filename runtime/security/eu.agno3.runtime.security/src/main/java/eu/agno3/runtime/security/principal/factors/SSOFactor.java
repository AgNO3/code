/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2015 by mbechler
 */
package eu.agno3.runtime.security.principal.factors;


import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.AuthFactorType;


/**
 * @author mbechler
 *
 */
public class SSOFactor implements AuthFactor {

    /**
     * 
     */
    private static final long serialVersionUID = -1530798439018533182L;

    private int countAsFactors = 1;


    /**
     * 
     */
    public SSOFactor () {}


    /**
     * @param countAsFactory
     * 
     */
    public SSOFactor ( int countAsFactory ) {
        this.countAsFactors = countAsFactory;
    }


    /**
     * @return the countAsFactors
     */
    public int getCountAsFactors () {
        return this.countAsFactors;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.AuthFactor#getFactorType()
     */
    @Override
    public AuthFactorType getFactorType () {
        return AuthFactorType.SSO;
    }

}
