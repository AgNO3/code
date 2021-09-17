/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.04.2014 by mbechler
 */
package eu.agno3.orchestrator.types.net.name;


import java.io.Serializable;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.agno3.orchestrator.types.net.AbstractIPAddress;
import eu.agno3.orchestrator.types.net.IPv6Address;
import eu.agno3.orchestrator.types.net.NetworkAddress;


/**
 * @author mbechler
 * 
 */
@XmlJavaTypeAdapter ( value = HostOrAddressXmlAdapter.class )
public class HostOrAddress implements Serializable {

    private static final long serialVersionUID = 4388180843440879679L;
    private NetworkAddress addr;
    private String hostName;


    /**
     * 
     */
    public HostOrAddress () {}


    /**
     * @param addr
     */
    public HostOrAddress ( NetworkAddress addr ) {
        this.addr = addr;
    }


    /**
     * @param hostName
     */
    public HostOrAddress ( String hostName ) {
        this.hostName = hostName;
    }


    /**
     * @return whether this is a hostname
     */
    public boolean isHostName () {
        return this.addr == null;
    }


    /**
     * @return whether this is a network address
     */
    public boolean isNetworkAddress () {
        return this.addr != null;
    }


    /**
     * @return the address
     */
    public NetworkAddress getAddress () {
        return this.addr;
    }


    /**
     * @return the host name
     */
    public String getHostName () {
        return this.hostName;
    }


    /**
     * @return the string value
     */
    public String getValue () {
        return this.toString();
    }


    /**
     * @param value
     */
    public void setValue ( String value ) {
        if ( AbstractIPAddress.isIPAddress(value) ) {
            this.addr = AbstractIPAddress.parse(value);
        }
        else {

            this.hostName = value;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        if ( this.isNetworkAddress() && this.getAddress() instanceof IPv6Address ) {
            return String.format("[%s]", this.getAddress().getCanonicalForm()); //$NON-NLS-1$
        }
        else if ( this.isNetworkAddress() ) {
            return this.getAddress().getCanonicalForm();
        }

        return this.getHostName();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        if ( this.isNetworkAddress() ) {
            return 5 * this.getAddress().hashCode();
        }

        return this.hostName != null ? 3 * this.getHostName().hashCode() : 0;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof HostOrAddress ) {
            HostOrAddress other = (HostOrAddress) obj;
            if ( this.isNetworkAddress() ^ other.isNetworkAddress() ) {
                return false;
            }

            if ( this.isNetworkAddress() ) {
                return this.addr.equals(other.addr);
            }

            return hostNameEquals(other);
        }

        return super.equals(obj);
    }


    private boolean hostNameEquals ( HostOrAddress other ) {
        if ( this.hostName == null && other.hostName == null ) {
            return true;
        }
        else if ( this.hostName == null ^ other.hostName == null ) {
            return false;
        }

        return this.hostName.equals(other.hostName);
    }


    /**
     * Parses typical address or host specifications into a HostOrAddress object
     * 
     * @param spec
     * @return the parsed address
     */
    public static HostOrAddress fromString ( String spec ) {
        HostOrAddress res = new HostOrAddress();
        res.setValue(spec);
        return res;
    }
}
