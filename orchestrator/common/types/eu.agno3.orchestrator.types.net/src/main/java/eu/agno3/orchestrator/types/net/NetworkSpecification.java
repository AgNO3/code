/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.03.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net;


import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 * 
 */
public class NetworkSpecification implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 8280628038739466442L;
    private NetworkAddress ifAddress;
    private short prefixLength;


    /**
     * 
     */
    public NetworkSpecification () {}


    /**
     * 
     * @param ifAddress
     * @param prefixLength
     */
    public NetworkSpecification ( NetworkAddress ifAddress, short prefixLength ) {
        this.ifAddress = ifAddress;
        this.prefixLength = prefixLength;
    }


    /**
     * 
     * @return the address
     */
    public NetworkAddress getAddress () {
        return this.ifAddress;
    }


    /**
     * 
     * @param ifAddress
     */
    public void setAddress ( NetworkAddress ifAddress ) {
        this.ifAddress = ifAddress;
    }


    /**
     * 
     * @return the prefix length
     */
    public short getPrefixLength () {
        return this.prefixLength;
    }


    /**
     * 
     * @param prefixLength
     */
    public void setPrefixLength ( short prefixLength ) {
        this.prefixLength = prefixLength;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof NetworkSpecification ) {
            NetworkSpecification o = (NetworkSpecification) obj;
            if ( this.prefixLength != o.getPrefixLength() ) {
                return false;
            }

            if ( this.ifAddress == null && o.getAddress() == null ) {
                return true;
            }
            else if ( this.ifAddress == null ) {
                return false;
            }
            return this.ifAddress.equals(o.getAddress());
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return 23 * this.prefixLength + ( this.ifAddress != null ? 7 * this.ifAddress.hashCode() : 0 );
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.ifAddress != null ) {
            return String.format("%s/%d", this.ifAddress.getCanonicalForm(), this.prefixLength); //$NON-NLS-1$ 
        }
        return StringUtils.EMPTY;
    }


    /**
     * @param s
     * @param allowNoPrefix
     * @return a parsed network specification
     */
    public static NetworkSpecification fromString ( String s, boolean allowNoPrefix ) {
        int slashPos = s.indexOf('/');

        if ( slashPos < 0 && allowNoPrefix ) {
            NetworkSpecification spec = new NetworkSpecification();
            AbstractIPAddress addr = AbstractIPAddress.parse(s);
            spec.setAddress(addr);
            spec.setPrefixLength((short) addr.getBitSize());
            return spec;
        }
        else if ( slashPos < 0 ) {
            throw new IllegalArgumentException("Not a valid network specification"); //$NON-NLS-1$
        }

        String addrStr = s.substring(0, slashPos);
        String prefixStr = s.substring(slashPos + 1);

        NetworkSpecification spec = new NetworkSpecification();
        spec.setAddress(AbstractIPAddress.parse(addrStr));
        spec.setPrefixLength(Short.parseShort(prefixStr));
        return spec;
    }
}
