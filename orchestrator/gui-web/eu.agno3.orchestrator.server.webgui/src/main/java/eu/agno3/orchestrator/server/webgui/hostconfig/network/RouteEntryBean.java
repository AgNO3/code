/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.07.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.FacesException;
import javax.inject.Named;

import eu.agno3.orchestrator.config.hostconfig.network.RouteType;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntry;
import eu.agno3.orchestrator.config.hostconfig.network.StaticRouteEntryImpl;


/**
 * @author mbechler
 * 
 */
@Named ( "routeEntryBean" )
@ApplicationScoped
public class RouteEntryBean {

    public List<RouteType> getRouteTypes () {
        return Arrays.asList(RouteType.values());
    }


    public Comparator<StaticRouteEntry> getComparator () {
        return new RouteEntryComparator();
    }


    public Object readOnlyRouteDisplay ( Object val ) {

        if ( ! ( val instanceof StaticRouteEntry ) ) {
            return val;
        }

        StaticRouteEntry route = (StaticRouteEntry) val;

        StringBuilder buf = new StringBuilder();

        if ( route.getTarget() != null ) {
            buf.append(route.getTarget());
        }

        if ( route.getGateway() != null ) {
            buf.append(" via "); //$NON-NLS-1$
            buf.append(route.getGateway());
        }

        if ( route.getDevice() != null ) {
            buf.append(" dev "); //$NON-NLS-1$
            buf.append(route.getDevice());
        }

        if ( route.getRouteType() != null && route.getRouteType() != RouteType.UNICAST ) {
            buf.append('(');
            buf.append(route.getRouteType().name());
            buf.append(')');
        }

        return buf.toString();
    }


    public Object clone ( Object obj ) {
        if ( ! ( obj instanceof StaticRouteEntry ) ) {
            throw new FacesException("Called clone on wrong type"); //$NON-NLS-1$
        }

        StaticRouteEntry toClone = (StaticRouteEntry) obj;

        StaticRouteEntryImpl entry = new StaticRouteEntryImpl();
        entry.setDevice(toClone.getDevice());
        entry.setAdvmss(toClone.getAdvmss());
        entry.setGateway(toClone.getGateway());
        entry.setTarget(toClone.getTarget());
        entry.setMtu(toClone.getMtu());
        entry.setRouteType(toClone.getRouteType() != null ? toClone.getRouteType() : RouteType.UNICAST);
        return entry;
    }


    public boolean gatewayDisabledFor ( RouteType e ) {
        if ( e == RouteType.UNICAST ) {
            return false;
        }

        return true;
    }


    public boolean deviceDisabledFor ( RouteType e ) {
        if ( e == RouteType.UNICAST || e == RouteType.BROADCAST ) {
            return false;
        }

        return true;
    }


    public boolean hasAdvancedOptions ( StaticRouteEntry e ) {
        if ( e.getAdvmss() != null || e.getMtu() != null || e.getSourceAddress() != null ) {
            return true;
        }

        return false;
    }


    public StaticRouteEntry createNew () {
        StaticRouteEntryImpl instance = new StaticRouteEntryImpl();
        instance.setRouteType(RouteType.UNICAST);
        return instance;
    }
}
