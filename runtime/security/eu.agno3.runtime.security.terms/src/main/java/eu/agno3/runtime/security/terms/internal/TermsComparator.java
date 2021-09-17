/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.09.2016 by mbechler
 */
package eu.agno3.runtime.security.terms.internal;


import java.util.Comparator;

import eu.agno3.runtime.security.terms.TermsDefinition;


/**
 * @author mbechler
 *
 */
public class TermsComparator implements Comparator<TermsDefinition> {

    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( TermsDefinition o1, TermsDefinition o2 ) {
        float p1 = o1.getPriority();
        float p2 = o2.getPriority();
        int res = Float.compare(p1, p2);
        if ( res != 0 ) {
            return res;
        }
        return Integer.compare(System.identityHashCode(o1), System.identityHashCode(o2));
    }

}
