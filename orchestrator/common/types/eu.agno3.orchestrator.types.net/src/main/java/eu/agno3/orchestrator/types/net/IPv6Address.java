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
public class IPv6Address extends AbstractIPAddress implements Comparable<IPv6Address> {

    /**
     * 
     */
    private static final long serialVersionUID = 3955358510362057320L;
    private short[] address;
    private String scopeSpec;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getBitSize()
     */
    @Override
    public int getBitSize () {
        return 16 * 8;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#getAddress()
     */
    @Override
    public short[] getAddress () {
        return Arrays.copyOf(this.address, 16);
    }


    /**
     * @return the scope specifier (interface name)
     */
    public String getScopeSpec () {
        return this.scopeSpec;
    }


    /**
     * @param scopeSpec
     *            the scopeSpec to set
     */
    public void setScopeSpec ( String scopeSpec ) {
        this.scopeSpec = scopeSpec;
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

        return IpUtil.toStringV6(this.address, this.scopeSpec);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo ( IPv6Address o ) {

        int res = Util.lexicalCompare(this.address, o.address);

        if ( res == 0 ) {
            return compareScopeSpecs(o);
        }
        return res;
    }


    private int compareScopeSpecs ( IPv6Address o ) {
        if ( this.scopeSpec == null && o.scopeSpec == null ) {
            return 0;
        }
        else if ( this.scopeSpec == null ) {
            return -1;
        }

        return this.scopeSpec.compareTo(o.scopeSpec);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof IPv6Address ) {
            IPv6Address o = (IPv6Address) obj;

            if ( this.address == null && o.address == null ) {
                return true;
            }
            else if ( this.address == null ^ o.address == null ) {
                return false;
            }

            if ( !Arrays.equals(this.address, o.address) ) {
                return false;
            }

            return equalsScopeSpec(o);
        }

        return super.equals(obj);
    }


    private boolean equalsScopeSpec ( IPv6Address o ) {
        if ( this.scopeSpec == null && o.scopeSpec == null ) {
            return true;
        }
        else if ( this.scopeSpec == null ) {
            return false;
        }

        return this.scopeSpec.equals(o.getScopeSpec());
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return ( this.address != null ? 7 * Arrays.hashCode(this.address) : 0 ) + ( this.scopeSpec != null ? 23 * this.scopeSpec.hashCode() : 0 );
    }


    /**
     * @param addr
     */
    public void setAddress ( short[] addr ) {
        if ( addr.length != 16 ) {
            throw new IllegalArgumentException("IPv6 Address must be 16 bytes long"); //$NON-NLS-1$
        }

        for ( int i = 0; i < 16; i++ ) {
            if ( addr[ i ] < 0 || addr[ i ] > 0xFF ) {
                throw new IllegalArgumentException("Illegal address, not a byte value"); //$NON-NLS-1$
            }
        }

        this.address = Arrays.copyOf(addr, 16);
    }


    /**
     * 
     * @param addr
     */
    public void fromString ( String addr ) {
        this.address = IpUtil.parseV6(addr);
        this.scopeSpec = IpUtil.parseV6Scope(addr);
    }


    /**
     * 
     * @param address
     * @return the parsed IPv6 address
     */
    public static IPv6Address parseV6Address ( String address ) {
        IPv6Address addr = new IPv6Address();
        addr.fromString(address);
        return addr;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isLoopback()
     */
    @Override
    public boolean isLoopback () {
        if ( this.address == null ) {
            return false;
        }

        for ( int i = 0; i < 15; i++ ) {
            if ( this.address[ i ] != 0 ) {
                return false;
            }
        }

        return this.address[ 15 ] == 1;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isUnspecified()
     */
    @Override
    public boolean isUnspecified () {
        if ( this.address == null ) {
            return false;
        }

        for ( int i = 0; i < 16; i++ ) {
            if ( this.address[ i ] != 0 ) {
                return false;
            }
        }

        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isBroadcast()
     */
    @Override
    public boolean isBroadcast () {
        return false;
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
        return this.address != null && ( this.address[ 0 ] & 0xFF00 ) == 0xFF00;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.types.net.NetworkAddress#isReserved()
     */
    @Override
    public boolean isReserved () {
        return this.address != null && ( isDocumentation() );
    }


    /**
     * @return
     */
    private boolean isDocumentation () {
        return this.address[ 0 ] == 0x2001 && this.address[ 1 ] == 0xDB8;
    }

}
