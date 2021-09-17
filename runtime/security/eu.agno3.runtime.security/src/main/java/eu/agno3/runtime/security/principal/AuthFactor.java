/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.principal;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public interface AuthFactor extends Serializable {

    /**
     * 
     * @return the factor type
     */
    AuthFactorType getFactorType ();
}
