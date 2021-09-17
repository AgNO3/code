/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.11.2014 by mbechler
 */
package eu.agno3.orchestrator.agent.realms;


import java.security.cert.Certificate;
import java.security.interfaces.ECPublicKey;


/**
 * @author mbechler
 *
 */
public class ECKeyEntry extends AbstractAsymmetricKeyEntry {

    /**
     * 
     */
    private static final long serialVersionUID = 6491072087019135399L;


    /**
     * 
     */
    public ECKeyEntry () {}


    /**
     * @param alias
     * @param pubKey
     * @param chain
     */
    public ECKeyEntry ( String alias, ECPublicKey pubKey, Certificate[] chain ) {
        super(alias, pubKey, chain);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.agent.realms.AbstractAsymmetricKeyEntry#getPublicKey()
     */
    @Override
    public ECPublicKey getPublicKey () {
        return (ECPublicKey) super.getPublicKey();
    }
}
