/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.03.2015 by mbechler
 */
package eu.agno3.runtime.security.password;


/**
 * @author mbechler
 *
 */
public interface PasswordEntropyEstimator {

    /**
     * 
     * @param password
     * @return the estimated password entropy in bits
     */
    int estimateEntropy ( String password );
}
