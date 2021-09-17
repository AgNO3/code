/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 * 
 */
public class MACAddress implements HardwareAddress, Comparable<MACAddress> {

    private static final long serialVersionUID = 8973492625464341001L;
    private static final String MAC_DELIMITER = ":"; //$NON-NLS-1$
    private short[] address;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.HardwareAddress#getAddress()
     */
    @Override
    public short[] getAddress () {
        return Arrays.copyOf(this.address, 6);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.HardwareAddress#getCanonicalForm()
     */
    @Override
    public String getCanonicalForm () {
        return this.toString();
    }


    /**
     * @return whether this is a unicast address
     */
    public boolean isUnicast () {
        if ( this.address == null ) {
            return false;
        }
        return ( this.address[ 0 ] & 0x1 ) == 0;
    }


    /**
     * @return whether this a multicast address
     */
    public boolean isMulticast () {
        if ( this.address == null ) {
            return false;
        }
        return ( this.address[ 0 ] & 0x1 ) == 0x1;
    }


    /**
     * @return whether this is the broadcast address
     */
    public boolean isBroadcast () {
        if ( this.address == null ) {
            return false;
        }
        for ( int i = 0; i < 6; i++ ) {
            if ( this.address[ 0 ] != 0xFF ) {
                return false;
            }
        }
        return true;
    }


    /**
     * @return whether this is a locally administered address
     */
    public boolean isLocallyAdministered () {
        if ( this.address == null ) {
            return false;
        }
        return ( this.address[ 0 ] & 0x2 ) == 0x2;
    }


    /**
     * 
     * @return whether this is a globally unique address
     */
    public boolean isGloballyUnique () {
        if ( this.address == null ) {
            return false;
        }
        return ( this.address[ 0 ] & 0x2 ) == 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.address == null ) {
            return StringUtils.EMPTY;
        }
        List<String> components = new ArrayList<>();

        for ( int i = 0; i < 6; i++ ) {
            components.add(String.format("%02x", this.address[ i ])); //$NON-NLS-1$
        }

        return StringUtils.join(components, MAC_DELIMITER);
    }


    /**
     * @param addr
     *            in transmission order
     */
    public void fromBytes ( short[] addr ) {
        if ( addr.length != 6 ) {
            throw new IllegalArgumentException("MACAddress must have 6 byte length"); //$NON-NLS-1$
        }

        for ( int i = 0; i < 6; i++ ) {
            if ( addr[ i ] < 0 || addr[ i ] > 0xff ) {
                throw new IllegalArgumentException("Each component must be one byte wide, got:" + addr[ i ]); //$NON-NLS-1$
            }
        }

        this.address = Arrays.copyOf(addr, 6);
    }


    /**
     * Convert from JAVAs representation
     * 
     * @param addr
     *            in transmission order
     */
    public void fromByteArray ( byte[] addr ) {
        short[] unsignedAddr = new short[6];

        for ( int i = 0; i < 6; i++ ) {
            if ( addr[ i ] < 0 ) {
                unsignedAddr[ i ] = (short) ( 0xFF + addr[ i ] + 1 );
            }
            else {
                unsignedAddr[ i ] = addr[ i ];
            }
        }

        this.fromBytes(unsignedAddr);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return Arrays.hashCode(this.address);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof MACAddress ) {
            MACAddress o = (MACAddress) obj;

            if ( this.address == null && o.address == null ) {
                return true;
            }
            else if ( this.address == null ^ o.address == null ) {
                return false;
            }

            return Arrays.equals(this.address, o.address);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( MACAddress o ) {
        return Util.lexicalCompare(this.address, o.address);
    }


    /**
     * Parse a mac address
     * 
     * Expected format is 6 colon-separated hex bytes
     * 
     * @param addr
     */
    public void fromString ( String addr ) {
        short[] parsed = new short[6];

        try ( Scanner scanner = new Scanner(addr) ) {
            scanner.useDelimiter(MAC_DELIMITER);
            for ( int i = 0; i < 6; i++ ) {
                short p = scanner.nextShort(16);

                if ( p < 0 || p > 0xff ) {
                    throw new IllegalArgumentException("Component is not a hex-byte"); //$NON-NLS-1$
                }
                parsed[ i ] = p;
            }

            if ( scanner.hasNext() ) {
                throw new IllegalArgumentException("Illegal address, trailing garbage"); //$NON-NLS-1$
            }
        }

        this.address = parsed;
    }
}
