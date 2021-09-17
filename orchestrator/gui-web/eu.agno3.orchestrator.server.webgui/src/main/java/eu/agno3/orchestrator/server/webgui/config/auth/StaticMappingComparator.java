/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.auth.StaticRoleMapEntry;


/**
 * @author mbechler
 *
 */
public class StaticMappingComparator implements Comparator<StaticRoleMapEntry>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1814942901632609242L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( StaticRoleMapEntry o1, StaticRoleMapEntry o2 ) {
        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return 1;
        }
        else if ( o2 == null ) {
            return -1;
        }

        if ( o1.getInstance() == null && o2.getInstance() == null ) {
            return 0;
        }
        else if ( o1.getInstance() == null ) {
            return 1;
        }
        else if ( o2.getInstance() == null ) {
            return -1;
        }

        return o1.getInstance().compareTo(o2.getInstance());
    }

}
