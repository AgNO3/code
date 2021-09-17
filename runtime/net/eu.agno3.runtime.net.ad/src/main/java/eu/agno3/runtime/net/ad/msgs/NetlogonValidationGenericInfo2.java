/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.03.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;


/**
 * @author mbechler
 *
 */
public class NetlogonValidationGenericInfo2 extends ValidationInformation {

    private byte[] data;


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.ad.msgs.ValidationInformation#getLevel()
     */
    @Override
    public int getLevel () {
        return 5;
    }


    /**
     * @return the data
     */
    public byte[] getData () {
        return this.data;
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer dst ) throws NdrException {

    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer src ) throws NdrException {
        int length = src.dec_ndr_long();
        this.data = new byte[length];

        int ptr = src.dec_ndr_long();

        if ( ptr != 0 ) {
            src.advance(4);
            src.readOctetArray(this.data, 0, length);
        }
    }

}
