/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrSetInformationUser2 extends DcerpcMessage {

    private policy_handle userHandle;

    private SamrUserInformation userInformation;


    /**
     * @param userHandle
     * @param userInformation
     * 
     */
    public SamrSetInformationUser2 ( policy_handle userHandle, SamrUserInformation userInformation ) {
        this.userHandle = userHandle;
        this.userInformation = userInformation;
        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getResult()
     */
    @Override
    public DcerpcException getResult () {
        return super.getResult();
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 58;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.userHandle.encode(buf);
        buf.enc_ndr_short(this.userInformation.getLevel());
        this.userInformation.encode(buf);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.result = buf.dec_ndr_long();
    }

}
