/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import java.util.Arrays;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;


/**
 * @author mbechler
 *
 */
public class NetlogonAuthenticator extends NdrObject {

    private byte[] credential;
    private int timestamp;


    /**
     * 
     */
    public NetlogonAuthenticator () {
        this.credential = new byte[8];
    }


    /**
     * @param credential
     * @param timestamp
     */
    public NetlogonAuthenticator ( byte[] credential, int timestamp ) {
        this.credential = Arrays.copyOf(credential, 8);
        this.timestamp = timestamp;
    }


    /**
     * @return the credential
     */
    public byte[] getCredential () {
        return this.credential;
    }


    /**
     * @return the timestamp
     */
    public int getTimestamp () {
        return this.timestamp;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {
        buf.align(4);
        buf.readOctetArray(this.credential, 0, 8);
        this.timestamp = buf.dec_ndr_long();
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {
        buf.align(4);
        buf.writeOctetArray(this.credential, 0, 8);
        buf.enc_ndr_long(this.timestamp);
    }

}
