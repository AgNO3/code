/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetrLogonGetTrustRid extends DcerpcMessage {

    private String serverName;
    private String domainName;
    private int rid;


    /**
     * @param serverName
     * @param domainName
     * 
     */
    public NetrLogonGetTrustRid ( String serverName, String domainName ) {
        this.serverName = serverName;
        this.domainName = domainName;

        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    /**
     * @return the rid
     */
    public int getRid () {
        return this.rid;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.rid = buf.dec_ndr_long();
        this.result = buf.dec_ndr_long();
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
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 23;
    }

}
