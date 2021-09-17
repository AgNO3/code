/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.03.2015 by mbechler
 */
package eu.agno3.runtime.security.principal.factors;


import eu.agno3.runtime.security.principal.AuthFactor;
import eu.agno3.runtime.security.principal.AuthFactorType;


/**
 * @author mbechler
 *
 */
public class TokenFactor implements AuthFactor {

    /**
     * 
     */
    private static final long serialVersionUID = -2453478983479127308L;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.AuthFactor#getFactorType()
     */
    @Override
    public AuthFactorType getFactorType () {
        return AuthFactorType.TOKEN;
    }

}
