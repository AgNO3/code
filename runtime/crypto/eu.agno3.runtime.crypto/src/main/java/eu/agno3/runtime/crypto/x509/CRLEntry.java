/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.x509;


import java.math.BigInteger;

import org.bouncycastle.asn1.x509.Extensions;
import org.joda.time.DateTime;


/**
 * @author mbechler
 *
 */
public interface CRLEntry {

    /**
     * @return the serial number of the revoked certificate
     */
    BigInteger getSerial ();


    /**
     * @return the revocation date
     */
    DateTime getRevocationDate ();


    /**
     * @return the entry extensions
     */
    Extensions getExtensions ();

}
