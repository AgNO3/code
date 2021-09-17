/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.11.2014 by mbechler
 */
package eu.agno3.runtime.crypto.x509.internal;


import java.io.IOException;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.CRLReason;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.Extensions;
import org.joda.time.DateTime;

import eu.agno3.runtime.crypto.x509.CRLEntry;


/**
 * @author mbechler
 *
 */
public class CRLEntryImpl implements CRLEntry {

    private DateTime revocationDate;
    private BigInteger serial;
    private int reason = CRLReason.unspecified;
    private Extensions exts;


    /**
     * @param serial
     * 
     */
    public CRLEntryImpl ( BigInteger serial ) {
        this.serial = serial;
        this.revocationDate = DateTime.now();
    }


    /**
     * @param serial
     * @param revocationDate
     * 
     */
    public CRLEntryImpl ( BigInteger serial, DateTime revocationDate ) {
        this.serial = serial;
        this.revocationDate = revocationDate;
    }


    /**
     * @param serial
     * @param revocationDate
     * @param reason
     * 
     */
    public CRLEntryImpl ( BigInteger serial, DateTime revocationDate, int reason ) {
        this.serial = serial;
        this.revocationDate = revocationDate;
        this.reason = reason;
    }


    /**
     * @param serial
     * @param revocationDate
     * @param exts
     */
    public CRLEntryImpl ( BigInteger serial, DateTime revocationDate, Extensions exts ) {
        this.serial = serial;
        this.revocationDate = revocationDate;
        this.exts = exts;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CRLEntry#getSerial()
     */
    @Override
    public BigInteger getSerial () {
        return this.serial;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CRLEntry#getRevocationDate()
     */
    @Override
    public DateTime getRevocationDate () {
        return this.revocationDate;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.crypto.x509.CRLEntry#getExtensions()
     */
    @Override
    public Extensions getExtensions () {
        if ( this.exts != null ) {
            return this.exts;
        }
        return new Extensions(this.getExtensionInternal());
    }


    /**
     * @return
     */
    private Extension[] getExtensionInternal () {
        try {
            return new Extension[] {
                this.getReasonExtension(), this.getInvalidityDateExtension()
            };
        }
        catch ( IOException e ) {
            throw new IllegalArgumentException(e);
        }
    }


    /**
     * @return
     * @throws IOException
     */
    private Extension getInvalidityDateExtension () throws IOException {
        return new Extension(Extension.invalidityDate, false, new DEROctetString(
            new ASN1GeneralizedTime(this.revocationDate.toDate()).toASN1Primitive()));
    }


    /**
     * @return
     * @throws IOException
     */
    private Extension getReasonExtension () throws IOException {
        return new Extension(Extension.reasonCode, false, new DEROctetString(CRLReason.lookup(this.reason).toASN1Primitive()));
    }

}
