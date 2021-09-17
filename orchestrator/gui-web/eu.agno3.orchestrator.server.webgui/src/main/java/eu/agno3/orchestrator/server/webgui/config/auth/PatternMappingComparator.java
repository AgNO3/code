/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.07.2015 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config.auth;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.config.auth.PatternRoleMapEntry;


/**
 * @author mbechler
 *
 */
public class PatternMappingComparator implements Comparator<PatternRoleMapEntry>, Serializable {

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
    public int compare ( PatternRoleMapEntry o1, PatternRoleMapEntry o2 ) {
        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return 1;
        }
        else if ( o2 == null ) {
            return -1;
        }

        if ( o1.getPattern() == null && o2.getPattern() == null ) {
            return 0;
        }
        else if ( o1.getPattern() == null ) {
            return 1;
        }
        else if ( o2.getPattern() == null ) {
            return -1;
        }

        return o1.getPattern().compareTo(o2.getPattern());
    }

}
