/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrLookupDomainInSamServer extends DcerpcMessage {

    private policy_handle handle;
    private UnicodeString domainName;

    private rpc.sid_t sid = new rpc.sid_t();


    /**
     * @param policy_handle
     * @param domainName
     * 
     */
    public SamrLookupDomainInSamServer ( policy_handle policy_handle, String domainName ) {
        this.handle = policy_handle;
        this.domainName = new UnicodeString(domainName, false);
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
        return 5;
    }


    /**
     * @return the sid
     */
    public rpc.sid_t getSid () {
        return this.sid;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.handle.encode(buf);
        this.domainName.encode(buf);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        int ptr = buf.dec_ndr_long();
        if ( ptr != 0 ) {
            this.sid.decode(buf);
        }
        this.result = buf.dec_ndr_long();
    }

}
