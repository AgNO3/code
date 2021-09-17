/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.04.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.instance.sysinfo;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.system.info.network.NetworkInterface;


/**
 * Sorts NetworkInterface instances by their interfaceIndex
 * 
 * @author mbechler
 * 
 */
class InterfaceIndexComparator implements Comparator<NetworkInterface>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4987832232761370104L;


    /**
     * 
     */
    public InterfaceIndexComparator () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( NetworkInterface o1, NetworkInterface o2 ) {
        return Integer.compare(o1.getInterfaceIndex(), o2.getInterfaceIndex());
    }

}