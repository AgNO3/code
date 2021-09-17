/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.util.ip.IpUtil;


/**
 * @author mbechler
 * 
 */
public class IPv4Address extends AbstractIPAddress implements Comparable<IPv4Address> {

    /**
     * 
     */
    private static final long serialVersionUID = 768237588004142931L;
    private short[] address;


    /**
     * 
     */
    public IPv4Address () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getBitSize()
     */
    @Override
    public int getBitSize () {
        return 4 * 8;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getAddress()
     */
    @Override
    public short[] getAddress () {
        if ( this.address == null ) {
            return null;
        }
        return Arrays.copyOf(this.address, 4);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getCanonicalForm()
     */
    @Override
    public String getCanonicalForm () {
        if ( this.address == null ) {
            return StringUtils.EMPTY;
        }
        return IpUtil.toString(this.address);
    }


    /**
     * 
     * @return the address in V6 mapped format
     */
    public String getV6Mapped () {
        if ( this.address == null ) {
            return StringUtils.EMPTY;
        }
        return String.format("::ffff:%s", this.getCanonicalForm()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getReadableForm()
     */
    @Override
    public String getReadableForm () {
        return this.getCanonicalForm();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        if ( this.address == null ) {
            return 0;
        }
        return Arrays.hashCode(this.address);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof IPv4Address ) {
            IPv4Address other = (IPv4Address) obj;

            if ( this.address == null && other.address == null ) {
                return true;
            }
            else if ( this.address == null ^ other.address == null ) {
                return false;
            }

            return Arrays.equals(this.address, other.address);
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * Lexicographic ordering
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( IPv4Address o ) {
        return Util.lexicalCompare(this.address, o.address);
    }


    /**
     * @param addr
     */
    public void setAddress ( short[] addr ) {
        if ( addr.length != 4 ) {
            throw new IllegalArgumentException("IPv4 Address must be 4 bytes long"); //$NON-NLS-1$
        }

        for ( int i = 0; i < 4; i++ ) {
            if ( addr[ i ] < 0 || addr[ i ] > 0xFF ) {
                throw new IllegalArgumentException("Illegal address, component must be byte valued"); //$NON-NLS-1$
            }
        }

        this.address = Arrays.copyOf(addr, 4);
    }


    /**
     * @param addr
     */
    public void fromString ( String addr ) {
        this.address = IpUtil.parseRegularV4(addr);
    }


    /**
     * 
     * @param address
     * @return the parsed v4 address
     * @throws IllegalArgumentException
     *             if the address cannot be parsed
     */
    public static IPv4Address parseV4Address ( String address ) {
        return (IPv4Address) AbstractIPAddress.fromBytes(IpUtil.parseV4(address));
    }


    /**
     * 
     * @param address
     * @return the parsed v4 address
     */
    public static IPv4Address parseRegularV4Address ( String address ) {
        return (IPv4Address) AbstractIPAddress.fromBytes(IpUtil.parseRegularV4(address));
    }


    /**
     * 
     * @param address
     * @return the parsed v6 mapped v4 address
     */
    public static IPv4Address parseV6MappedV4Address ( String address ) {
        return (IPv4Address) AbstractIPAddress.fromBytes(IpUtil.parseV6MappedV4Address(address));
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isLoopback()
     */
    @Override
    public boolean isLoopback () {
        return this.address != null && this.address[ 0 ] == 127;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isUnspecified()
     */
    @Override
    public boolean isUnspecified () {
        return this.address[ 0 ] == 0 && this.address[ 1 ] == 0 && this.address[ 2 ] == 0 && this.address[ 3 ] == 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isBroadcast()
     */
    @Override
    public boolean isBroadcast () {
        return this.address != null && ( this.address[ 0 ] == 0 || isLimitedBroadcast() );
    }


    private boolean isLimitedBroadcast () {
        return this.address[ 0 ] == 255 && this.address[ 1 ] == 255 && this.address[ 2 ] == 255 && this.address[ 3 ] == 255;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isAnycast()
     */
    @Override
    public boolean isAnycast () {
        return false;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isMulticast()
     */
    @Override
    public boolean isMulticast () {
        return this.address != null && this.address[ 0 ] == 224;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isReserved()
     */
    @Override
    public boolean isReserved () {
        return this.address != null && ( isTestNET() || isFutureReserved() );
    }


    /**
     * @return
     */
    private boolean isFutureReserved () {
        return ( this.address[ 0 ] & 0xF0 ) == 240 && this.address[ 3 ] != 255;
    }


    /**
     * @return
     */
    private boolean isTestNET () {
        return isTestNET1() || isTestNET2() || isTestNET3();
    }


    private boolean isTestNET1 () {
        return this.address[ 0 ] == 192 && this.address[ 1 ] == 0 && this.address[ 2 ] == 2;
    }


    private boolean isTestNET2 () {
        return this.address[ 0 ] == 198 && this.address[ 1 ] == 51 && this.address[ 2 ] == 100;
    }


    private boolean isTestNET3 () {
        return this.address[ 0 ] == 203 && this.address[ 1 ] == 0 && this.address[ 2 ] == 113;
    }

}
