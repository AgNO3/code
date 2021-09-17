/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import java.security.SecureRandom;
import java.util.Arrays;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetrJoinDomain2 extends DcerpcMessage {

    private String serverName;
    private String domainName;
    private String machineAccountOU;
    private String accountName;

    private byte[] encPassword;
    private int options;


    /**
     * @param serverName
     * @param domainName
     * @param machineAccountOU
     * @param accountName
     * @param encPassword
     * @param options
     */
    public NetrJoinDomain2 ( String serverName, String domainName, String machineAccountOU, String accountName, byte[] encPassword, int options ) {
        super();
        this.serverName = serverName;
        this.domainName = domainName;
        this.machineAccountOU = machineAccountOU;
        this.accountName = accountName;
        if ( encPassword != null ) {
            this.encPassword = Arrays.copyOf(encPassword, encPassword.length);
        }
        this.options = options;

        this.ptype = 0;
        this.flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer ndrBuffer ) throws NdrException {

    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer ndrBuffer ) throws NdrException {
        // PrimaryHandle

        ndrBuffer.enc_ndr_referent(this.serverName, 1);
        ndrBuffer.enc_ndr_string(this.serverName);

        ndrBuffer.enc_ndr_string(this.domainName);

        ndrBuffer.enc_ndr_referent(null, 1);
        if ( this.machineAccountOU != null ) {
            ndrBuffer.enc_ndr_string(this.machineAccountOU);
        }

        ndrBuffer.enc_ndr_referent(this.accountName, 1);
        ndrBuffer.enc_ndr_string(this.accountName);

        ndrBuffer.enc_ndr_referent(this.encPassword, 1);

        byte[] obfuscator = new byte[8];
        ( new SecureRandom() ).nextBytes(obfuscator);
        byte[] padded = new byte[512];
        System.arraycopy(this.encPassword, 0, padded, padded.length - this.encPassword.length, this.encPassword.length);

        ndrBuffer.writeOctetArray(obfuscator, 0, 8);
        ndrBuffer.writeOctetArray(padded, 0, 512);
        ndrBuffer.enc_ndr_long(this.encPassword.length);

        ndrBuffer.enc_ndr_long(this.options);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#getOpnum()
     */
    @Override
    public int getOpnum () {
        return 22;
    }

}
