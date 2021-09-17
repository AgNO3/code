/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 9, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


/**
 * @author mbechler
 *
 */
public interface ExtendedHostnameVerifier {

    /**
     * @return whether to bypass trust manager endpoint identification checks
     */
    boolean isBypassBuiltinChecks ();

}
