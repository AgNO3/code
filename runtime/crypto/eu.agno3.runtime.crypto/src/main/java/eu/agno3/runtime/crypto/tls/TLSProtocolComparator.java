/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.io.Serializable;
import java.util.Comparator;

import org.osgi.framework.Version;


/**
 * @author mbechler
 *
 */
public class TLSProtocolComparator implements Comparator<String>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -3509886194734903457L;
    /**
     * 
     */
    private static final String SSL_V2_HELLO = "SSLv2Hello"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String TLS = "TLSv"; //$NON-NLS-1$
    private static final String SSL = "SSLv"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( String o1, String o2 ) {

        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        if ( o1.startsWith(TLS) && o2.startsWith(TLS) ) {
            return compareTLSVersions(o1, o2);
        }
        else if ( o1.startsWith(TLS) ) {
            return 1;
        }
        else if ( o2.startsWith(TLS) ) {
            return -1;
        }

        if ( o1.startsWith(SSL) && o2.startsWith(SSL) ) {
            return compareSSLVersions(o1, o2);
        }
        return 0;
    }


    /**
     * @param o1
     * @param o2
     * @return
     */
    private static int compareSSLVersions ( String o1, String o2 ) {
        if ( o1.equals(SSL_V2_HELLO) && o2.equals(SSL_V2_HELLO) ) {
            return 0;
        }
        else if ( o1.equals(SSL_V2_HELLO) ) {
            return -1;
        }
        else if ( o2.equals(SSL_V2_HELLO) ) {
            return 1;
        }
        Version v1 = Version.parseVersion(o1.substring(SSL.length()));
        Version v2 = Version.parseVersion(o2.substring(SSL.length()));
        return -1 * v1.compareTo(v2);
    }


    /**
     * @param o1
     * @param o2
     */
    private static int compareTLSVersions ( String o1, String o2 ) {
        Version v1 = Version.parseVersion(o1.substring(TLS.length()));
        Version v2 = Version.parseVersion(o2.substring(TLS.length()));
        return -1 * v1.compareTo(v2);
    }

}
