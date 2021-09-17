/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.util.ArrayList;
import java.util.List;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrLookupNamesInDomain extends DcerpcMessage {

    private policy_handle domainHandle;
    private int count;
    private List<String> names;

    private List<Integer> relativeIds = new ArrayList<>();
    private List<Integer> use = new ArrayList<>();


    /**
     * @param domainHandle
     * @param count
     * @param names
     */
    public SamrLookupNamesInDomain ( policy_handle domainHandle, int count, List<String> names ) {
        this.domainHandle = domainHandle;
        this.count = count;
        this.names = names;

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
        return 17;
    }


    /**
     * @return the relativeIds
     */
    public List<Integer> getRelativeIds () {
        return this.relativeIds;
    }


    /**
     * @return the use
     */
    public List<Integer> getUse () {
        return this.use;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {

        this.domainHandle.encode(buf);
        buf.enc_ndr_long(this.count);

        buf.enc_ndr_long(1000);
        buf.enc_ndr_long(0);
        buf.enc_ndr_long(this.count);

        for ( String name : this.names ) {
            UnicodeString ucs = new UnicodeString(name, false);
            ucs.encode(buf);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {

        int countRids = buf.dec_ndr_long();

        for ( int i = 0; i < countRids; i++ ) {
            buf.advance(8);
            this.relativeIds.add(buf.dec_ndr_long());
        }

        int countUse = buf.dec_ndr_long();
        for ( int i = 0; i < countUse; i++ ) {
            buf.advance(8);
            this.relativeIds.add(buf.dec_ndr_long());
        }
    }

}
