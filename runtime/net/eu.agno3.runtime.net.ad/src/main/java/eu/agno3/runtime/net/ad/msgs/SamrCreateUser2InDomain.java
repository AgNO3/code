/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrCreateUser2InDomain extends DcerpcMessage {

    private policy_handle domainHandle;
    private UnicodeString name;
    private int accountType;
    private int desiredAccess;

    private policy_handle userHandle = new policy_handle();
    private int grantedAccess;
    private int relativeId;


    /**
     * @param domainHandle
     * @param name
     * @param accountType
     * @param desiredAccess
     * 
     */
    public SamrCreateUser2InDomain ( policy_handle domainHandle, String name, int accountType, int desiredAccess ) {
        this.domainHandle = domainHandle;
        this.name = new UnicodeString(name, false);
        this.accountType = accountType;
        this.desiredAccess = desiredAccess;

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
        return 50;
    }


    /**
     * @return the userHandle
     */
    public policy_handle getUserHandle () {
        return this.userHandle;
    }


    /**
     * @return the grantedAccess
     */
    public int getGrantedAccess () {
        return this.grantedAccess;
    }


    /**
     * @return the relativeId
     */
    public int getRelativeId () {
        return this.relativeId;
    }


    /**
     * 
     * @return whether the user existed
     */
    public boolean existed () {
        return this.result == 0xc0000063;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.domainHandle.encode(buf);
        this.name.encode(buf);
        buf.enc_ndr_long(this.accountType);
        buf.enc_ndr_long(this.desiredAccess);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        this.userHandle.decode(buf);
        this.grantedAccess = buf.dec_ndr_long();
        this.relativeId = buf.dec_ndr_long();

        this.result = buf.dec_ndr_long();
    }

}
