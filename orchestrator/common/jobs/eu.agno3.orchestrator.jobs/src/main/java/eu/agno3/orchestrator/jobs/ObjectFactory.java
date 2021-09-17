/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs;


/**
 * @author mbechler
 * 
 */
public class ObjectFactory {

    /**
     * 
     * @return the default implementation
     */
    public Job createJob () {
        return new JobImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public JobInfo createJobInfo () {
        return new JobInfoImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public JobStatusInfo createJobStatusInfo () {
        return new JobStatusInfoImpl();
    }


    /**
     * 
     * @return the default implementation
     */
    public JobProgressInfo createJobProgressInfo () {
        return new JobProgressInfoImpl();
    }
}
