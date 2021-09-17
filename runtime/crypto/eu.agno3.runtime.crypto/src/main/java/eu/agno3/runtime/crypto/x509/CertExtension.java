/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.x509;


import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;


/**
 * @author mbechler
 *
 */
public interface CertExtension {

    /**
     * @return extension OID
     */
    ASN1ObjectIdentifier getObjectIdentifier ();


    /**
     * 
     * @return critical extension
     */
    boolean isCritical ();


    /**
     * 
     * @return encodable extension value
     */
    ASN1Encodable getExtension ();
}
