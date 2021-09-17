/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
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
public class SamrQueryDomainInfo extends DcerpcMessage {

    private policy_handle bindingHandle;
    private DomainInformation information;


    /**
     * @param handle
     * @param domInfo
     * 
     */
    public SamrQueryDomainInfo ( policy_handle handle, DomainInformation domInfo ) {
        this.bindingHandle = handle;
        this.information = domInfo;

        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    /**
     * @return the passwordInformation
     */
    public DomainInformation getInformation () {
        return this.information;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        if ( buf.dec_ndr_long() != 0 ) {
            int domInfoClass = buf.dec_ndr_short();

            if ( domInfoClass != this.information.getDomainInformationClass() ) {
                throw new NdrException("Mismatched domain information class"); //$NON-NLS-1$
            }

            this.information.decode(buf);
        }
        this.result = buf.dec_ndr_long();
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        this.bindingHandle.encode(buf);
        buf.enc_ndr_short(this.information.getDomainInformationClass());
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 46;
    }

}
