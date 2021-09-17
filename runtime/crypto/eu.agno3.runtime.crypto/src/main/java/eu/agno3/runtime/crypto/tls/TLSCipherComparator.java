/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.07.2015 by mbechler
 */
package eu.agno3.runtime.crypto.tls;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 *
 */
public class TLSCipherComparator implements Comparator<String>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7990560667549795524L;

    /**
     * 
     */
    private static final String ECDHE = "ECDHE_"; //$NON-NLS-1$

    private static final String DHE = "DHE_"; //$NON-NLS-1$

    /**
     * 
     */
    private static final String SSL = "SSL_"; //$NON-NLS-1$
    /**
     * 
     */
    private static final String TLS = "TLS_"; //$NON-NLS-1$

    private static final String EXPORT = "EXPORT_"; //$NON-NLS-1$

    private static final String AES = "AES_"; //$NON-NLS-1$


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( String o1, String o2 ) {

        if ( o1.startsWith(TLS) && o2.startsWith(TLS) ) {
            return sortPFS(o1.substring(TLS.length()), o2.substring(TLS.length()));
        }
        else if ( o1.startsWith(TLS) ) {
            return -1;
        }
        else if ( o2.startsWith(TLS) ) {
            return 1;
        }
        else if ( o1.startsWith(SSL) && o2.startsWith(SSL) ) {
            return sortPFS(o1.substring(SSL.length()), o2.substring(SSL.length()));
        }
        else if ( o1.startsWith(SSL) ) {
            return -1;
        }
        else if ( o2.startsWith(SSL) ) {
            return 1;
        }

        return 0;
    }


    /**
     * @return
     */
    private static int sortPFS ( String o1, String o2 ) {

        if ( o1.startsWith(ECDHE) && o2.startsWith(ECDHE) ) {
            return sortCipher(o1.substring(o1.indexOf('_', ECDHE.length() + 5) + 1), o2.substring(o2.indexOf('_', ECDHE.length() + 5) + 1));
        }
        else if ( o1.startsWith(DHE) && o2.startsWith(DHE) ) {
            return sortCipher(o1.substring(o1.indexOf('_', DHE.length() + 5) + 1), o2.substring(o2.indexOf('_', DHE.length() + 5) + 1));
        }
        else if ( o1.startsWith(ECDHE) ) {
            return -1;
        }
        else if ( o2.startsWith(ECDHE) ) {
            return 1;
        }
        else if ( o1.startsWith(DHE) ) {
            return -1;
        }
        else if ( o2.startsWith(DHE) ) {
            return 1;
        }
        else if ( o1.startsWith(EXPORT) && o2.startsWith(EXPORT) ) {
            return sortCipher(o1.substring(o1.indexOf('_', EXPORT.length() + 5) + 1), o2.substring(o2.indexOf('_', EXPORT.length() + 5) + 1));
        }
        else if ( o1.startsWith(EXPORT) ) {
            return 1;
        }
        else if ( o2.startsWith(EXPORT) ) {
            return -1;
        }

        return sortCipher(o1.substring(o1.indexOf('_', 5) + 1), o2.substring(o2.indexOf('_', 5) + 1));
    }


    /**
     * 
     * @return
     */
    private static int sortCipher ( String o1, String o2 ) {
        if ( o1.startsWith(AES) && o2.startsWith(AES) ) {
            return o1.compareTo(o2);
        }
        else if ( o1.startsWith(AES) ) {
            return -1;
        }
        else if ( o2.startsWith(AES) ) {
            return 1;
        }
        return o1.compareTo(o2);
    }

}
