/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.util.Arrays;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetrServerReqChallenge extends DcerpcMessage {

    private String server;
    private String localNetbiosHostname;
    private byte[] clientChallenge;
    private byte[] serverChallenge = new byte[8];


    /**
     * @param server
     * @param localNetbiosHostname
     * @param clientChallenge
     */
    public NetrServerReqChallenge ( String server, String localNetbiosHostname, byte[] clientChallenge ) {
        this.server = server;
        this.localNetbiosHostname = localNetbiosHostname;
        this.clientChallenge = Arrays.copyOf(clientChallenge, 8);

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
        return 4;
    }


    /**
     * @return the server challenge
     */
    public byte[] getServerChallenge () {
        return this.serverChallenge;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        buf.readOctetArray(this.serverChallenge, 0, 8);
        this.result = buf.dec_ndr_long();
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {
        buf.enc_ndr_referent(this.server, 1);
        buf.enc_ndr_string(this.server);
        buf.enc_ndr_string(this.localNetbiosHostname);
        buf.writeOctetArray(this.clientChallenge, 0, 8);
    }

}
