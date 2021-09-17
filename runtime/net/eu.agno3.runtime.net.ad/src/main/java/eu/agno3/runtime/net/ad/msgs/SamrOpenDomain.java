/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.rpc;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.rpc.sid_t;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrOpenDomain extends DcerpcMessage {

    private policy_handle serverHandle;

    private int desiredAccess;

    private rpc.sid_t sid;

    private policy_handle domainHandle = new policy_handle();


    /**
     * @param policy_handle
     * @param desiredAccess
     * @param sid
     * 
     */
    public SamrOpenDomain ( policy_handle policy_handle, int desiredAccess, sid_t sid ) {
        this.serverHandle = policy_handle;
        this.desiredAccess = desiredAccess;
        this.sid = sid;
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
        return 7;
    }


    /**
     * @return the domainHandle
     */
    public policy_handle getDomainHandle () {
        return this.domainHandle;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.serverHandle.encode(buf);
        buf.enc_ndr_long(this.desiredAccess);
        this.sid.encode(buf);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.domainHandle.decode(buf);
    }

}
