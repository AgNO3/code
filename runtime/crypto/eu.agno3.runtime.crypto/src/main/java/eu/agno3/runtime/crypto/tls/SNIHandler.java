/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 18, 2016 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.List;

import javax.net.ssl.SNIServerName;


/**
 * @author mbechler
 *
 */
public interface SNIHandler {

    /**
     * @return priority, lower is matched earlier
     */
    float getPriority ();


    /**
     * 
     * @return an identifier for the handler
     */
    String getHandlerId ();


    /**
     * 
     * @param requestedServerNames
     * @return an id that will be passed to the chain and private key methods, or null if no match was found
     */
    String matches ( List<SNIServerName> requestedServerNames );


    /**
     * @param id
     * @return certificate chain to use for the given id
     */
    X509Certificate[] getX509CertificateChain ( String id );


    /**
     * 
     * @param id
     * @return private key to use
     */
    PrivateKey getPrivateKey ( String id );
}
