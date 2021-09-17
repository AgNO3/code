/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;


/**
 * @author mbechler
 *
 */
@Component ( service = HostnameVerifier.class, property = "instanceId=noVerify" )
public class NoHostnameVerifier implements HostnameVerifier {

    private static final Logger log = Logger.getLogger(NoHostnameVerifier.class);


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    public boolean verify ( String hostname, SSLSession session ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Not performing hostname verification for " + hostname); //$NON-NLS-1$
        }
        return true;
    }

}
