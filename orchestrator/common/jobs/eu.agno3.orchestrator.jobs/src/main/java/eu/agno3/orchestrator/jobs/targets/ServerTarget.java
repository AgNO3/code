/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.04.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.targets;


import java.util.UUID;

import eu.agno3.orchestrator.jobs.JobTarget;


/**
 * @author mbechler
 * 
 */
public class ServerTarget implements JobTarget {

    protected static final String SERVER_PREFIX = "server:"; //$NON-NLS-1$
    private static final long serialVersionUID = -6279386671131908794L;

    private UUID serverId;


    /**
     * @param serverId
     */
    public ServerTarget ( UUID serverId ) {
        this.serverId = serverId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return SERVER_PREFIX + this.serverId;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {
        if ( obj instanceof ServerTarget ) {
            return this.serverId.equals( ( (ServerTarget) obj ).serverId);
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
        return this.serverId.hashCode();
    }
}
