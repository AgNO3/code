/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.UnicodeString;
import jcifs.dcerpc.rpc;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;


/**
 * @author mbechler
 *
 */
public class NetlogonIdentityInfo extends NdrObject {

    private UnicodeString logonDomainName;
    private int parameterControl;
    private UnicodeString userName;
    private UnicodeString workstation;


    /**
     * @param logonDomainName
     * @param parameterControl
     * @param reservedLow
     * @param reservedHigh
     * @param userName
     * @param workstation
     */
    public NetlogonIdentityInfo ( String logonDomainName, int parameterControl, int reservedLow, int reservedHigh, String userName, String workstation ) {
        this.logonDomainName = new UnicodeString(logonDomainName, false);
        this.parameterControl = parameterControl;
        this.userName = new UnicodeString(userName, false);
        this.workstation = new UnicodeString(workstation, false);
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {

    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {
        buf.enc_ndr_short(this.logonDomainName.length);
        buf.enc_ndr_short(this.logonDomainName.maximum_length);
        buf.enc_ndr_referent(this.logonDomainName.buffer, 1);
        buf.enc_ndr_long(this.parameterControl);
        buf.enc_ndr_long(0);
        buf.enc_ndr_long(0);
        buf.enc_ndr_short(this.userName.length);
        buf.enc_ndr_short(this.userName.maximum_length);
        buf.enc_ndr_referent(this.userName.buffer, 1);
        buf.enc_ndr_short(this.workstation.length);
        buf.enc_ndr_short(this.workstation.maximum_length);
        buf.enc_ndr_referent(this.workstation.buffer, 1);
    }


    /**
     * @param dst
     */
    public void encodeData ( NdrBuffer dst ) {
        encodeString(dst, this.logonDomainName);
        encodeString(dst, this.userName);
        encodeString(dst, this.workstation);
    }


    private static void encodeString ( NdrBuffer buf, rpc.unicode_string string ) {
        NdrBuffer bufDeffered = buf.deferred;
        int stringBufferl = string.length / 2;
        int stringBuffers = string.maximum_length / 2;
        bufDeffered.enc_ndr_long(stringBuffers);
        bufDeffered.enc_ndr_long(0);
        bufDeffered.enc_ndr_long(stringBufferl);
        int stringBufferIndex = bufDeffered.index;
        bufDeffered.advance(2 * stringBufferl);
        NdrBuffer bufDerived = buf.derive(stringBufferIndex);
        for ( int _i = 0; _i < stringBufferl; _i++ ) {
            bufDerived.enc_ndr_short(string.buffer[ _i ]);
        }
    }

}
