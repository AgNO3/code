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
public class DsrGetSiteName extends DcerpcMessage {

    private String serverName;
    private String siteName;
    private int rid;


    /**
     * @param serverName
     * @param domainName
     * 
     */
    public DsrGetSiteName ( String serverName, String domainName ) {
        this.serverName = serverName;

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
        buf.advance(4);
        this.siteName = buf.dec_ndr_string();
        this.result = buf.dec_ndr_long();
    }


    /**
     * @return the siteName
     */
    public String getSiteName () {
        return this.siteName;
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
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 28;
    }

}
