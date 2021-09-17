/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.util.Arrays;

import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetlogonGenericInformation extends LogonInformation {

    private NetlogonIdentityInfo logonIdentity;
    private UnicodeString packageName;
    private byte[] data;


    /**
     * @param logonIdentity
     * @param packageName
     * @param data
     * 
     */
    public NetlogonGenericInformation ( NetlogonIdentityInfo logonIdentity, String packageName, byte[] data ) {
        this.logonIdentity = logonIdentity;
        this.packageName = new UnicodeString(packageName, false);
        if ( data != null ) {
            this.data = Arrays.copyOf(data, data.length);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.LogonInformation#getLevel()
     */
    @Override
    public int getLevel () {
        return 4;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer dst ) throws NdrException {

        dst.align(4);
        dst.enc_ndr_referent(this, 1);

        this.logonIdentity.encode(dst);

        dst.enc_ndr_short(this.packageName.length);
        dst.enc_ndr_short(this.packageName.maximum_length);
        dst.enc_ndr_referent(this.packageName, 1);

        dst.enc_ndr_long(this.data.length);
        dst.enc_ndr_referent(this.data, 1);

        this.logonIdentity.encodeData(dst);

        this.encodeUnicodeString(dst, this.packageName);

        dst.enc_ndr_long(this.data.length);
        dst.writeOctetArray(this.data, 0, this.data.length);
    }


    protected void encodeUnicodeString ( NdrBuffer ndrbuf, rpc.unicode_string string ) {
        NdrBuffer buf = ndrbuf.deferred;
        int stringBufferl = string.length / 2;
        int stringBuffers = string.maximum_length / 2;

        buf.enc_ndr_long(stringBuffers);
        buf.enc_ndr_long(0);
        buf.enc_ndr_long(stringBufferl);

        int stringBufferIndex = buf.index;

        buf.advance(2 * stringBufferl);

        buf = buf.derive(stringBufferIndex);

        for ( int _i = 0; _i < stringBufferl; _i++ ) {
            buf.enc_ndr_short(string.buffer[ _i ]);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer src ) throws NdrException {

    }

}
