/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.targets;


import eu.agno3.orchestrator.jobs.JobTarget;


/**
 * @author mbechler
 * 
 */
public class AnyServerTarget implements JobTarget {

    protected static final String SERVERS = "servers"; //$NON-NLS-1$
    private static final long serialVersionUID = -6398734596251146540L;


    /**
     * 
     */
    public AnyServerTarget () {

    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return SERVERS;
    }


    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof AnyServerTarget ) {
            return true;
        }
        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.toString().hashCode();
    }
}
