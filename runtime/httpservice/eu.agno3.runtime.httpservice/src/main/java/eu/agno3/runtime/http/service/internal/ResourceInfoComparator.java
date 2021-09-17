/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.internal;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import eu.agno3.runtime.http.service.resource.ResourceDescriptor;

class ResourceInfoComparator implements Comparator<ResourceDescriptor>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 528603626782829221L;


    /**
     * 
     */
    public ResourceInfoComparator () {}


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( ResourceDescriptor arg0, ResourceDescriptor arg1 ) {

        // priority first
        int prio1 = arg0.getPriority();
        int prio2 = arg1.getPriority();

        int res = Integer.compare(prio1, prio2);

        if ( res == 0 ) {
            int comps1 = StringUtils.countMatches(arg0.getPath(), "/"); //$NON-NLS-1$
            int comps2 = StringUtils.countMatches(arg1.getPath(), "/"); //$NON-NLS-1$

            res = Integer.compare(comps1, comps2);
        }

        if ( res == 0 ) {
            return arg0.getPath().compareTo(arg1.getPath());
        }

        return 0;
    }

}