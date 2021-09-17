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
public class NetrServerAuthenticate3 extends DcerpcMessage {

    private String server;
    private String account;
    private String localNetbiosHostname;
    private byte[] clientCredential;
    private byte[] serverCredential = new byte[8];
    private int negotiatedFlags;
    private int secureChannelType;
    private int accountRid;


    /**
     * @param server
     * @param account
     * @param secureChannelType
     * @param localNetbiosHostname
     * @param clientCredential
     * @param negotiatedFlags
     */
    public NetrServerAuthenticate3 ( String server, String account, int secureChannelType, String localNetbiosHostname, byte[] clientCredential,
            int negotiatedFlags ) {
        this.server = server;
        this.account = account;
        this.secureChannelType = secureChannelType;
        this.localNetbiosHostname = localNetbiosHostname;
        if ( clientCredential != null ) {
            this.clientCredential = Arrays.copyOf(clientCredential, clientCredential.length);
        }
        this.negotiatedFlags = negotiatedFlags;

        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    @Override
    public int getOpnum () {
        return 26;
    }


    /**
     * @return the serverCredential
     */
    public byte[] getServerCredential () {
        if ( this.serverCredential != null ) {
            return Arrays.copyOf(this.serverCredential, this.serverCredential.length);
        }
        return null;
    }


    /**
     * @return the accountRid
     */
    public int getAccountRid () {
        return this.accountRid;
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
        buf.enc_ndr_string(this.account);
        buf.enc_ndr_short(this.secureChannelType);
        buf.enc_ndr_string(this.localNetbiosHostname);
        buf.writeOctetArray(this.clientCredential, 0, 8);
        buf.enc_ndr_long(this.negotiatedFlags);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        buf.readOctetArray(this.serverCredential, 0, 8);
        this.negotiatedFlags = buf.dec_ndr_long();
        this.accountRid = buf.dec_ndr_long();
        this.result = buf.dec_ndr_long();
    }

}
