/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.eclipse.equinox.p2.core.UIServices;


/**
 * @author mbechler
 *
 */
public class UIServicesImpl extends UIServices {

    private static final Logger log = Logger.getLogger(UIServicesImpl.class);


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.UIServices#getTrustInfo(java.security.cert.Certificate[][], java.lang.String[])
     */
    @Override
    public TrustInfo getTrustInfo ( Certificate[][] chains, String[] arg1 ) {
        log.error("Untrusted certificates:"); //$NON-NLS-1$
        for ( Certificate[] chain : chains ) {
            if ( chain[ 0 ] instanceof X509Certificate ) {
                log.error("Untrusted signer: " + ( (X509Certificate) chain[ 0 ] ).getSubjectX500Principal()); //$NON-NLS-1$
            }
        }
        return new TrustInfo(new Certificate[] {}, false, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.UIServices#getUsernamePassword(java.lang.String)
     */
    @Override
    public AuthenticationInfo getUsernamePassword ( String arg0 ) {
        return null;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.equinox.p2.core.UIServices#getUsernamePassword(java.lang.String,
     *      org.eclipse.equinox.p2.core.UIServices.AuthenticationInfo)
     */
    @Override
    public AuthenticationInfo getUsernamePassword ( String arg0, AuthenticationInfo arg1 ) {
        return null;
    }

}
