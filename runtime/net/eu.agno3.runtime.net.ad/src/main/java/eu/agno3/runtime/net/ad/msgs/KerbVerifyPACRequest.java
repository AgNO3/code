/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.pac.Pac;
import jcifs.util.Encdec;


/**
 * @author mbechler
 *
 */
public class KerbVerifyPACRequest {

    private int messageType = 0x3;

    private int signatureType;

    private byte[] checksum;
    private byte[] signature;


    /**
     * @param pac
     * 
     */
    public KerbVerifyPACRequest ( Pac pac ) {
        this.checksum = pac.getServerSignature().getChecksum();
        this.signature = pac.getKdcSignature().getChecksum();
        this.signatureType = pac.getKdcSignature().getType();
    }


    /**
     * @return the encoded data
     */
    public byte[] getEncoded () {
        byte data[] = new byte[16 + this.checksum.length + this.signature.length];
        int pos = 0;
        Encdec.enc_uint32le(this.messageType, data, pos);
        pos += 4;
        Encdec.enc_uint32le(this.checksum.length, data, pos);
        pos += 4;
        Encdec.enc_uint32le(this.signatureType, data, pos);
        pos += 4;
        Encdec.enc_uint32le(this.signature.length, data, pos);
        pos += 4;

        System.arraycopy(this.checksum, 0, data, pos, this.checksum.length);
        pos += this.checksum.length;
        System.arraycopy(this.signature, 0, data, pos, this.signature.length);
        return data;
    }
}
