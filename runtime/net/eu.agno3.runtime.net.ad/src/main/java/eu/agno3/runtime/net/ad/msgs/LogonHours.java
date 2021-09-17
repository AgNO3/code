/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.10.2015 by mbechler
 */
package eu.agno3.runtime.net.ad.msgs;


import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;


/**
 * @author mbechler
 *
 */
public class LogonHours extends NdrObject {

    private int unitsPerWeek;
    private byte[] bits;
    private int ptr;


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#decode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void decode ( NdrBuffer buf ) throws NdrException {
        this.unitsPerWeek = buf.dec_ndr_short();
        this.bits = new byte[this.unitsPerWeek / 8];

        this.ptr = buf.dec_ndr_long();
    }


    /**
     * @param buf
     */
    public void complete ( NdrBuffer buf ) {
        if ( this.ptr != 0 ) {
            buf.dec_ndr_long();
            buf.dec_ndr_long();
            buf.dec_ndr_long();
            buf.readOctetArray(this.bits, 0, this.unitsPerWeek / 8);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see jcifs.dcerpc.ndr.NdrObject#encode(jcifs.dcerpc.ndr.NdrBuffer)
     */
    @Override
    public void encode ( NdrBuffer buf ) throws NdrException {

    }

}
