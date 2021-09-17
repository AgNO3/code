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
public class CertExtensionImpl implements CertExtension {

    private ASN1ObjectIdentifier oid;
    private boolean critical;
    private ASN1Encodable extension;


    /**
     * @param oid
     * @param critical
     * @param extension
     */
    public CertExtensionImpl ( ASN1ObjectIdentifier oid, boolean critical, ASN1Encodable extension ) {
        super();
        this.oid = oid;
        this.critical = critical;
        this.extension = extension;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CertExtension#getObjectIdentifier()
     */
    @Override
    public ASN1ObjectIdentifier getObjectIdentifier () {
        return this.oid;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CertExtension#isCritical()
     */
    @Override
    public boolean isCritical () {
        return this.critical;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CertExtension#getExtension()
     */
    @Override
    public ASN1Encodable getExtension () {
        return this.extension;
    }

}
