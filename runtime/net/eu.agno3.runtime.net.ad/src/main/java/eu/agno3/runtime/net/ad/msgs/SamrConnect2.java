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
public class SamrConnect2 extends DcerpcMessage {

    private String serverName;
    private policy_handle serverHandle = new policy_handle();
    private int desiredAccess;


    /**
     * @param serverName
     * @param desiredAccess
     * 
     */
    public SamrConnect2 ( String serverName, int desiredAccess ) {
        this.desiredAccess = desiredAccess;
        this.serverName = serverName;

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
        return 57;
    }


    /**
     * @return the serverHandle
     */
    public policy_handle getServerHandle () {
        return this.serverHandle;
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
        buf.enc_ndr_long(this.desiredAccess);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.serverHandle.decode(buf);
    }
}
