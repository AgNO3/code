/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.jobs;


import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class UpdateRevertJob extends JobImpl {

    private String revertStream;
    private long revertSequence;


    /**
     * 
     */
    public UpdateRevertJob () {
        super(new UpdateJobGroup());
    }


    /**
     * @return the revertSequence
     */
    public long getRevertSequence () {
        return this.revertSequence;
    }


    /**
     * @param revertSequence
     *            the revertSequence to set
     */
    public void setRevertSequence ( long revertSequence ) {
        this.revertSequence = revertSequence;
    }


    /**
     * @return the revertStream
     */
    public String getRevertStream () {
        return this.revertStream;
    }


    /**
     * @param revertStream
     *            the revertStream to set
     */
    public void setRevertStream ( String revertStream ) {
        this.revertStream = revertStream;
    }
}
