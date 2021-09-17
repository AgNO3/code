/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.08.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.structure.menu.base;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 * 
 */
public class ActionMenuContributionComparator implements Comparator<ActionMenuContribution>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 269137632038462204L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( ActionMenuContribution o1, ActionMenuContribution o2 ) {

        int res = Float.compare(o1.getWeight(), o2.getWeight());

        if ( res == 0 ) {
            res = o1.getSource().getClass().getName().compareTo(o2.getSource().getClass().getName());
        }

        if ( res == 0 ) {
            return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
        }

        return res;
    }

}
