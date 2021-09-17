/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.util.Arrays;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetlogonNetworkInfo extends LogonInformation {

    private NetlogonIdentityInfo netlogonIdentityInfo;
    private byte[] serverChallenge;
    private byte[] ntResponse;
    private byte[] lmResponse;


    /**
     * @param netlogonIdentityInfo
     * @param serverChallenge
     * @param ntResponse
     * @param lmResponse
     */
    public NetlogonNetworkInfo ( NetlogonIdentityInfo netlogonIdentityInfo, byte[] serverChallenge, byte[] ntResponse, byte[] lmResponse ) {
        this.netlogonIdentityInfo = netlogonIdentityInfo;
        if ( serverChallenge != null ) {
            this.serverChallenge = Arrays.copyOf(serverChallenge, serverChallenge.length);
        }
        if ( ntResponse != null ) {
            this.ntResponse = Arrays.copyOf(ntResponse, ntResponse.length);
        }
        if ( lmResponse != null ) {
            this.lmResponse = Arrays.copyOf(lmResponse, lmResponse.length);
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.LogonInformation#getLevel()
     */
    @Override
    public int getLevel () {
        return 2;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer arg0 ) throws NdrException {}


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {
        buf.align(4);
        buf.enc_ndr_referent(this, 1);

        this.netlogonIdentityInfo.encode(buf);
        buf.writeOctetArray(this.serverChallenge, 0, 8);

        buf.enc_ndr_short((short) this.ntResponse.length);
        buf.enc_ndr_short((short) this.ntResponse.length);
        buf.enc_ndr_referent(this.ntResponse, 1);
        buf.enc_ndr_short((short) this.lmResponse.length);
        buf.enc_ndr_short((short) this.lmResponse.length);
        buf.enc_ndr_referent(this.lmResponse, 1);

        this.netlogonIdentityInfo.encodeData(buf);

        encodeResponse(buf, this.ntResponse);
        encodeResponse(buf, this.lmResponse);
    }


    /**
     * @param buf
     * @param ntResponse2
     */
    private static void encodeResponse ( NdrBuffer buf, byte[] response ) {
        buf.enc_ndr_long(response.length);
        buf.enc_ndr_long(0);
        buf.enc_ndr_long(response.length);
        buf.writeOctetArray(response, 0, response.length);
    }

}
