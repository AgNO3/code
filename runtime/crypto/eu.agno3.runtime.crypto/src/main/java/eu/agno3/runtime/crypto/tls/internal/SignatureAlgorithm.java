/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2014 by mbechler
 */
package eu.agno3.runtime.crypto.tls.internal;


/**
 * @author mbechler
 *
 */
@SuppressWarnings ( {
    "javadoc", "nls"
} )
public enum SignatureAlgorithm {

    MD2("1.2.840.113549.2.1"),
    MD2RSA("1.2.840.113549.1.1.2"),
    MD5("1.2.840.113549.2.5"),
    MD5RSA("1.2.840.113549.1.1.4"),
    SHA1("1.3.14.3.2.26"),
    SHA1DSA("1.2.840.10040.4.3"),
    SHA1ECDSA("1.2.840.10045.4.1"),
    SHA1RSA("1.2.840.113549.1.1.5"),
    SHA224("2.16.840.1.101.3.4.2.4"),
    SHA224RSA("1.2.840.113549.1.1.14"),
    SHA224ECDSA("1.2.840.10045.4.3.1"),
    SHA224DSA("2.16.840.1.101.3.4.3.1"),
    SHA256("2.16.840.1.101.3.4.2.1"),
    SHA256RSA("1.2.840.113549.1.1.11"),
    SHA256ECDSA("1.2.840.10045.4.3.2"),
    SHA56DSA("2.16.840.1.101.3.4.3.2"),
    SHA384("2.16.840.1.101.3.4.2.2"),
    SHA384RSA("1.2.840.113549.1.1.12"),
    SHA384ECDSA("1.2.840.10045.4.3.3"),
    SHA512("2.16.840.1.101.3.4.2.3"),
    SHA512RSA("1.2.840.113549.1.1.13"),
    SHA512ECDSA("1.2.840.10045.4.3.4");

    private String oid;


    SignatureAlgorithm ( String oid ) {
        this.oid = oid;
    }


    /**
     * @return the oid
     */
    public String getOid () {
        return this.oid;
    }
}
