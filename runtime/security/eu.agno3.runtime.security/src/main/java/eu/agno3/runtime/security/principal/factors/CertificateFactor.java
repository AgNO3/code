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
public class CertificateFactor implements AuthFactor {

    /**
     * 
     */
    private static final long serialVersionUID = 3613501368718416559L;

    private final int keySize;

    private final Boolean hardware;

    private final boolean onlineValidated;


    /**
     * @param keySize
     * @param hardware
     * @param onlineValidated
     * 
     */
    public CertificateFactor ( int keySize, Boolean hardware, boolean onlineValidated ) {
        this.keySize = keySize;
        this.hardware = hardware;
        this.onlineValidated = onlineValidated;
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.principal.AuthFactor#getFactorType()
     */
    @Override
    public AuthFactorType getFactorType () {
        return AuthFactorType.CERTIFICATE;
    }


    /**
     * @return the keySize
     */
    public int getKeySize () {
        return this.keySize;
    }


    /**
     * @return the hardware
     */
    public Boolean getHardware () {
        return this.hardware;
    }


    /**
     * @return the onlineValidated
     */
    public boolean isOnlineValidated () {
        return this.onlineValidated;
    }
}
