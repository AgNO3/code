/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.io.Serializable;

import eu.agno3.orchestrator.config.hostconfig.network.NetworkType;
import eu.agno3.orchestrator.config.hostconfig.network.RouteType;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry;
import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class RouteIdentifier implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 7479766794149854606L;
    private RouteType type;
    private NetworkSpecification target;
    private NetworkType ntype;
    private NetworkAddress gateway;


    /**
     * @return the type
     */
    public RouteType getType () {
        return this.type;
    }


    /**
     * @return the target
     */
    public NetworkSpecification getTarget () {
        return this.target;
    }


    /**
     * @param type
     * @param ntype
     * @param target
     * @param gateway
     */
    public RouteIdentifier ( RouteType type, NetworkType ntype, NetworkSpecification target, NetworkAddress gateway ) {
        super();
        this.type = type;
        this.ntype = ntype;
        this.target = target;
        this.gateway = gateway;
    }


    /**
     * @param e
     * 
     */
    public RouteIdentifier ( StaticRouteEntry e ) {
        this(e.getRouteType() != null ? e.getRouteType() : RouteType.UNICAST, makeNetworkType(e), e.getTarget(), e.getGateway());
    }


    /**
     * @param e
     * @return
     */
    private static NetworkType makeNetworkType ( StaticRouteEntry e ) {
        if ( e.getTarget() == null ) {
            return NetworkType.fromNetworkAddress(e.getGateway());
        }
        return NetworkType.fromNetworkAddress(e.getTarget().getAddress());
    }


    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.ntype == null ) ? 0 : this.ntype.hashCode() );
        result = prime * result + ( ( this.target == null ) ? 0 : this.target.hashCode() );
        result = prime * result + ( ( this.type == null ) ? 0 : this.type.hashCode() );
        return result;
    }
    // -GENERATED


    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        RouteIdentifier other = (RouteIdentifier) obj;
        if ( this.ntype != other.ntype )
            return false;
        if ( this.target == null ) {
            if ( other.target != null )
                return false;
        }
        else if ( !this.target.equals(other.target) )
            return false;
        if ( this.type != other.type )
            return false;
        return true;
    }
    // -GENERATED


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format("%s/%s: %s via %s", this.type, this.ntype, this.target, this.gateway); //$NON-NLS-1$
    }
}
