/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 * 
 */
@MapAs ( NetworkInformation.class )
public class NetworkInformationStorageImpl implements NetworkInformation, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5964496927614521597L;
    private List<NetworkInterface> networkInterfaces = new ArrayList<>();
    private List<RouteEntry> routes = new ArrayList<>();
    private List<NetworkAddress> dnsServers = new ArrayList<>();


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformation#getNetworkInterfaces()
     */
    @Override
    public List<NetworkInterface> getNetworkInterfaces () {
        return this.networkInterfaces;
    }


    /**
     * @param networkInterfaces
     *            the networkInterfaces to set
     */
    public void setNetworkInterfaces ( List<NetworkInterface> networkInterfaces ) {
        this.networkInterfaces = networkInterfaces;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformation#getRoutes()
     */
    @Override
    public List<RouteEntry> getRoutes () {
        return this.routes;
    }


    /**
     * @param routes
     *            the routes to set
     */
    public void setRoutes ( List<RouteEntry> routes ) {
        this.routes = routes;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.info.network.NetworkInformation#getDnsServers()
     */
    @Override
    public List<NetworkAddress> getDnsServers () {
        return this.dnsServers;
    }


    /**
     * @param dnsServers
     *            the dnsServers to set
     */
    public void setDnsServers ( List<NetworkAddress> dnsServers ) {
        this.dnsServers = dnsServers;
    }


    @Override
    // +GENERATED
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.networkInterfaces == null ) ? 0 : this.networkInterfaces.hashCode() );
        result = prime * result + ( ( this.routes == null ) ? 0 : this.routes.hashCode() );
        result = prime * result + ( ( this.dnsServers == null ) ? 0 : this.dnsServers.hashCode() );
        return result;
    }


    // -GENERATED

    @Override
    // +GENERATED
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( ! ( obj instanceof NetworkInformationStorageImpl ) )
            return false;
        NetworkInformationStorageImpl other = (NetworkInformationStorageImpl) obj;
        if ( this.networkInterfaces == null ) {
            if ( other.networkInterfaces != null )
                return false;
        }
        else if ( !this.networkInterfaces.equals(other.networkInterfaces) )
            return false;
        if ( this.routes == null ) {
            if ( other.routes != null )
                return false;
        }
        else if ( !this.routes.equals(other.routes) )
            return false;
        if ( this.dnsServers == null ) {
            if ( other.dnsServers != null )
                return false;
        }
        else if ( !this.dnsServers.equals(other.dnsServers) )
            return false;
        return true;
    }
    // -GENERATED

}
