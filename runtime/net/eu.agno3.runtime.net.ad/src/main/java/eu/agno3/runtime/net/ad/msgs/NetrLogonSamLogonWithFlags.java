/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import eu.agno3.runtime.net.ad.NetlogonAuthenticator;

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetrLogonSamLogonWithFlags extends DcerpcMessage {

    private String logonServer;
    private String computerName;

    private NetlogonAuthenticator authenticator;
    private NetlogonAuthenticator returnAuthenticator = new NetlogonAuthenticator();

    private LogonInformation logonInformation;
    private ValidationInformation validationInformation;
    private byte authorative;
    private int extraFlags;


    /**
     * @param logonServer
     * @param computerName
     * @param authenticator
     * @param info
     * @param val
     * @param extraFlags
     * 
     */
    public NetrLogonSamLogonWithFlags ( String logonServer, String computerName, NetlogonAuthenticator authenticator, LogonInformation info,
            ValidationInformation val, int extraFlags ) {
        this.logonServer = logonServer;
        this.computerName = computerName;
        this.authenticator = authenticator;

        this.logonInformation = info;
        this.validationInformation = val;
        this.extraFlags = extraFlags;

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
        return 45;
    }


    /**
     * @return the authorative
     */
    public byte getAuthorative () {
        return this.authorative;
    }


    /**
     * @return the returnAuthenticator
     */
    public NetlogonAuthenticator getReturnAuthenticator () {
        return this.returnAuthenticator;
    }


    /**
     * @return the validationInformation
     */
    public ValidationInformation getValidationInformation () {
        return this.validationInformation;
    }


    /**
     * @return the extraFlags
     */
    public int getExtraFlags () {
        return this.extraFlags;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#encode_in(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode_in ( NdrBuffer buf ) throws NdrException {

        buf.enc_ndr_referent(this.logonServer, 1);
        buf.enc_ndr_string(this.logonServer);

        buf.enc_ndr_referent(this.computerName, 1);
        buf.enc_ndr_string(this.computerName);

        buf.enc_ndr_referent(this.authenticator, 1);
        this.authenticator.encode(buf);

        buf.enc_ndr_referent(this.returnAuthenticator, 1);
        this.returnAuthenticator.encode(buf);

        buf.enc_ndr_short(this.logonInformation.getLevel());
        buf.enc_ndr_short(this.logonInformation.getLevel());

        this.logonInformation.encode(buf);

        buf.enc_ndr_short(this.validationInformation.getLevel());

        buf.enc_ndr_long(this.extraFlags);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.DcerpcMessage#decode_out(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode_out ( NdrBuffer buf ) throws NdrException {
        int auth = buf.dec_ndr_long();
        if ( auth != 0 ) {
            this.returnAuthenticator.decode(buf);
        }

        int type = buf.dec_ndr_short();

        if ( type != this.validationInformation.getLevel() ) {
            throw new NdrException("Validation level mismatch " + type); //$NON-NLS-1$
        }

        int res = buf.dec_ndr_long();
        if ( res != 0 ) {
            this.validationInformation.decode(buf);
        }
        this.authorative = (byte) buf.dec_ndr_small();
        this.extraFlags = buf.dec_ndr_long();

        this.result = buf.dec_ndr_long();
    }

}
