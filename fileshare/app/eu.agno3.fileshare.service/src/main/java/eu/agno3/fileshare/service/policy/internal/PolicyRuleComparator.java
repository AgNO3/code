/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.03.2015 by mbechler
 */
package eu.agno3.fileshare.service.policy.internal;


import java.io.Serializable;
import java.util.Comparator;


/**
 * @author mbechler
 *
 */
public class PolicyRuleComparator implements Comparator<PolicyRule>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1889358035007767894L;


    /**
     * {@inheritDoc}
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( PolicyRule a, PolicyRule b ) {
        return Float.compare(a.getPriority(), b.getPriority());
    }

}
