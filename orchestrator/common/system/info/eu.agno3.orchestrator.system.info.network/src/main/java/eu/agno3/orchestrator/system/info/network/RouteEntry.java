/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 10, 2016 by mbechler
 */
package eu.agno3.orchestrator.system.info.network;


import java.io.Serializable;
import java.util.Set;

import eu.agno3.orchestrator.types.net.NetworkAddress;
import eu.agno3.orchestrator.types.net.NetworkSpecification;


/**
 * @author mbechler
 *
 */
public class RouteEntry implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7498521819123668009L;

    private String interfaceName;

    private NetworkSpecification network;

    // V6 only
    private NetworkSpecification source;

    private NetworkAddress gateway;

    private int metric;

    private int ref;
    private int use;

    private Set<RouteFlags> flags;

    private int mtu;
    private int window;
    private int irtt;


    /**
     * @return the interfaceName
     */
    public String getInterfaceName () {
        return this.interfaceName;
    }


    /**
     * @param interfaceName
     *            the interfaceName to set
     */
    public void setInterfaceName ( String interfaceName ) {
        this.interfaceName = interfaceName;
    }


    /**
     * @return the network
     */
    public NetworkSpecification getNetwork () {
        return this.network;
    }


    /**
     * @param network
     *            the network to set
     */
    public void setNetwork ( NetworkSpecification network ) {
        this.network = network;
    }


    /**
     * @return the source
     */
    public NetworkSpecification getSource () {
        return this.source;
    }


    /**
     * @param source
     *            the source to set
     */
    public void setSource ( NetworkSpecification source ) {
        this.source = source;
    }


    /**
     * @return the gateway
     */
    public NetworkAddress getGateway () {
        return this.gateway;
    }


    /**
     * @param gateway
     *            the gateway to set
     */
    public void setGateway ( NetworkAddress gateway ) {
        this.gateway = gateway;
    }


    /**
     * @return the metric
     */
    public int getMetric () {
        return this.metric;
    }


    /**
     * @param metric
     *            the metric to set
     */
    public void setMetric ( int metric ) {
        this.metric = metric;
    }


    /**
     * @return the ref
     */
    public int getRef () {
        return this.ref;
    }


    /**
     * @param ref
     *            the ref to set
     */
    public void setRef ( int ref ) {
        this.ref = ref;
    }


    /**
     * @return the use
     */
    public int getUse () {
        return this.use;
    }


    /**
     * @param use
     *            the use to set
     */
    public void setUse ( int use ) {
        this.use = use;
    }


    /**
     * @return the flags
     */
    public Set<RouteFlags> getFlags () {
        return this.flags;
    }


    /**
     * @return the mtu
     */
    public int getMtu () {
        return this.mtu;
    }


    /**
     * @param mtu
     */
    public void setMtu ( int mtu ) {
        this.mtu = mtu;
    }


    /**
     * @return the window
     */
    public int getWindow () {
        return this.window;
    }


    /**
     * @param window
     *            the window to set
     */
    public void setWindow ( int window ) {
        this.window = window;
    }


    /**
     * @return the irtt
     */
    public int getIrtt () {
        return this.irtt;
    }


    /**
     * @param irtt
     *            the irtt to set
     */
    public void setIrtt ( int irtt ) {
        this.irtt = irtt;
    }


    /**
     * @param flags
     *            the flags to set
     */
    public void setFlags ( Set<RouteFlags> flags ) {
        this.flags = flags;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return String.format(
            "%s -> %s (%s)[flags=%s]", //$NON-NLS-1$
            this.network,
            this.gateway,
            this.interfaceName,
            this.flags);
    }

}
