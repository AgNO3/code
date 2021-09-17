/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 31.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.diff;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 * 
 */
public class DiffPostProcessorComparator implements Comparator<DiffPostProcessor>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -8538251961398548705L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( DiffPostProcessor arg0, DiffPostProcessor arg1 ) {

        if ( arg0.getPriority() == arg1.getPriority() ) {
            return Integer.compare(System.identityHashCode(arg0), System.identityHashCode(arg1));
        }

        return -1 * Integer.compare(arg0.getPriority(), arg1.getPriority());
    }

}
