/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class SamrUserControlInformation extends SamrUserInformation {

    private int accountFlags;


    /**
     * @param accountFlags
     * 
     */
    public SamrUserControlInformation ( int accountFlags ) {
        this.accountFlags = accountFlags;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.SamrUserInformation#getLevel()
     */
    @Override
    public short getLevel () {
        return 16;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer dst ) throws NdrException {
        dst.enc_ndr_short(this.getLevel());
        dst.enc_ndr_long(this.accountFlags);
    }

}
