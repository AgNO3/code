/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.server.service.impl;


import java.io.Serializable;
import java.util.Comparator;

import eu.agno3.orchestrator.jobs.JobInfo;


/**
 * @author mbechler
 * 
 */
public class JobInfoDisplayComparator implements Comparator<JobInfo>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7095396494597523100L;


    /**
     * {@inheritDoc}
     * 
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare ( JobInfo o1, JobInfo o2 ) {
        int res = -1 * o1.getQueuedTime().compareTo(o2.getQueuedTime());
        if ( res != 0 ) {
            return res;
        }

        res = o1.getState().compareTo(o2.getState());

        if ( res != 0 ) {
            return res;
        }

        return o1.getJobId().compareTo(o2.getJobId());
    }

}
