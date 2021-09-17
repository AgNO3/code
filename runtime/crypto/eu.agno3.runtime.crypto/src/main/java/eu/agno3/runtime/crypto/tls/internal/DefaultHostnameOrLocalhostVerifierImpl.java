/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier;


/**
 * @author mbechler
 *
 */
@Component ( service = HostnameVerifier.class, property = "instanceId=defaultOrLocalhost", configurationPid = "tls.verify" )
public class DefaultHostnameOrLocalhostVerifierImpl extends DefaultHostnameVerifierImpl implements ExtendedHostnameVerifier {

    private static final Logger log = Logger.getLogger(DefaultHostnameOrLocalhostVerifierImpl.class);
    private static final Set<String> LOCALHOST_HOSTNAMES = new HashSet<>();


    static {
        LOCALHOST_HOSTNAMES.add("localhost"); //$NON-NLS-1$
        LOCALHOST_HOSTNAMES.add("127.0.0.1"); //$NON-NLS-1$
        LOCALHOST_HOSTNAMES.add("::1"); //$NON-NLS-1$
    }


    @Override
    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    @Override
    @Modified
    protected synchronized void modified ( ComponentContext ctx ) {
        configure(ctx.getProperties());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.ExtendedHostnameVerifier#isBypassBuiltinChecks()
     */
    @Override
    public boolean isBypassBuiltinChecks () {
        // this is required, otherwise the JRE builtin checks will
        // already reject the certificate before reaching this verifier
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.net.ssl.HostnameVerifier#verify(java.lang.String, javax.net.ssl.SSLSession)
     */
    @Override
    public boolean verify ( String hostname, SSLSession session ) {

        if ( super.verify(hostname, session) ) {
            return true;
        }

        if ( LOCALHOST_HOSTNAMES.contains(hostname) ) {
            return true;
        }

        log.warn(String.format("Hostname '%s' does not match certificate", hostname)); //$NON-NLS-1$
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.tls.internal.DefaultHostnameVerifierImpl#notifyError(java.lang.String)
     */
    @Override
    protected void notifyError ( String hostname ) {
        // ignore
    }
}
