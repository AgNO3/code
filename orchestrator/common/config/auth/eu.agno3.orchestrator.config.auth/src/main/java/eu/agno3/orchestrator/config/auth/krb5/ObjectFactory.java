/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.auth.krb5;


/**
 * @author mbechler
 *
 */
public class ObjectFactory {

    /**
     * 
     * @return default impl
     */
    public KerberosAuthenticatorConfig makeKerberosAuthenticatorConfig () {
        return new KerberosAuthenticatorConfigImpl();
    }
}
