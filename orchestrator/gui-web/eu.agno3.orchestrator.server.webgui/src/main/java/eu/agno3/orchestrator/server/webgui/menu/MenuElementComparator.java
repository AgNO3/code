/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.menu;


import java.io.Serializable;
import java.util.Comparator;

import org.primefaces.model.menu.MenuItem;
import org.primefaces.model.menu.Submenu;


/**
 * @author mbechler
 *
 */
public class MenuElementComparator implements Comparator<WeightedMenuElement>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 405314245785963843L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( WeightedMenuElement o1, WeightedMenuElement o2 ) {

        if ( o1 == null && o2 == null ) {
            return 0;
        }
        else if ( o1 == null ) {
            return -1;
        }
        else if ( o2 == null ) {
            return 1;
        }

        return compareElements(o1, o2);
    }


    /**
     * @param o1
     * @param o2
     * @return
     */
    private static int compareElements ( WeightedMenuElement o1, WeightedMenuElement o2 ) {
        int compare = Float.compare(o1.getWeight(), o2.getWeight());
        if ( compare == 0 ) {
            if ( o1 instanceof Submenu && o2 instanceof Submenu ) {
                return nullSafeStringCompare( ( (Submenu) o1 ).getLabel(), ( (Submenu) o2 ).getLabel());
            }
            else if ( o1 instanceof MenuItem && o2 instanceof MenuItem ) {
                return nullSafeStringCompare( ( (MenuItem) o1 ).getTitle(), ( (MenuItem) o2 ).getTitle());
            }
            else if ( o1 instanceof Submenu ) {
                return -1;
            }
            else {
                return 1;
            }
        }
        return compare;
    }


    /**
     * @param a
     * @param b
     * @return
     */
    private static int nullSafeStringCompare ( String a, String b ) {
        if ( a == null && b == null ) {
            return 0;
        }
        else if ( a == null ) {
            return -1;
        }
        else if ( b == null ) {
            return 1;
        }
        return a.compareTo(b);
    }
}
