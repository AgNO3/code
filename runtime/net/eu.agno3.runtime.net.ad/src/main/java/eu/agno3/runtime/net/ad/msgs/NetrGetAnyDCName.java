/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetrGetAnyDCName extends DcerpcMessage {

    private String serverName;
    private String domainName;
    private UnicodeString dcName = new UnicodeString(false);


    /**
     * @param serverName
     * @param domainName
     * 
     */
    public NetrGetAnyDCName ( String serverName, String domainName ) {
        this.serverName = serverName;
        this.domainName = domainName;

        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 13;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        buf.enc_ndr_referent(this.serverName, 1);
        buf.enc_ndr_string(this.serverName);

        buf.enc_ndr_referent(this.domainName, 1);
        buf.enc_ndr_string(this.domainName);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.dcName.decode(buf);
        this.result = buf.dec_ndr_long();
    }

}
