/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.hostconfig.network;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.hostconfig.network.InterfaceEntry;


/**
 * @author mbechler
 *
 */
final class InterfaceEntryComparator implements Comparator<InterfaceEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4077229562101737433L;


    /**
     * 
     */
    public InterfaceEntryComparator () {}


    @Override
    public int compare ( InterfaceEntry a, InterfaceEntry b ) {

        if ( a.getId() == null && b.getId() == null ) {
            return 0;
        }
        else if ( a.getId() == null ) {
            return 1;
        }
        else if ( b.getId() == null ) {
            return -1;
        }

        return compareInterfaceIndex(a, b);

    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int compareInterfaceIndex ( InterfaceEntry a, InterfaceEntry b ) {
        if ( a.getInterfaceIndex() == null && b.getInterfaceIndex() == null ) {
            return 0;
        }
        else if ( a.getInterfaceIndex() == null ) {
            return 1;
        }
        else if ( b.getInterfaceIndex() == null ) {
            return -1;
        }

        return Integer.compare(a.getInterfaceIndex(), b.getInterfaceIndex());
    }
}