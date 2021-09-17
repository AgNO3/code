/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrOpenUser extends DcerpcMessage {

    private policy_handle domainHandle;
    private int desiredAccess = 0x1 | 0x20 | 0x00020010 | 0x0002000E | 0x00020021 | 1 << 25;
    private int userId;

    private policy_handle userHandle = new policy_handle();


    /**
     * @param domainHandle
     * @param desiredAccess
     * @param userId
     * 
     */
    public SamrOpenUser ( policy_handle domainHandle, int desiredAccess, int userId ) {
        this.domainHandle = domainHandle;
        this.desiredAccess = desiredAccess;
        this.userId = userId;
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
        return 34;
    }


    /**
     * @return the userHandle
     */
    public policy_handle getUserHandle () {
        return this.userHandle;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.domainHandle.encode(buf);
        buf.enc_ndr_long(this.desiredAccess);
        buf.enc_ndr_long(this.userId);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.userHandle.decode(buf);
    }

}
