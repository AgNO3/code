/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.compound;


import java.util.Arrays;
import java.util.List;

import eu.agno3.orchestrator.jobs.Job;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class CompoundJob extends JobImpl implements Job {

    private List<Job> jobs;
    private String name;
    private List<Float> weights;


    /**
     * 
     */
    public CompoundJob () {
        super();
    }


    /**
     * @param name
     * @param jobs
     */
    public CompoundJob ( String name, Job... jobs ) {
        super();
        this.name = name;
        this.jobs = Arrays.asList(jobs);
    }


    /**
     * @return the name
     */
    public String getName () {
        return this.name;
    }


    /**
     * @param name
     *            the name to set
     */
    public void setName ( String name ) {
        this.name = name;
    }


    /**
     * @return the jobs
     */
    public List<Job> getJobs () {
        return this.jobs;
    }


    /**
     * @param jobs
     *            the jobs to set
     */
    public void setJobs ( List<Job> jobs ) {
        this.jobs = jobs;
    }


    /**
     * @return the individual jobs weights for progress mapping
     */
    public List<Float> getWeights () {
        return this.weights;
    }


    /**
     * @param weights
     *            the weights to set (must total to 1.0)
     */
    public void setWeights ( List<Float> weights ) {
        this.weights = weights;
    }

}
