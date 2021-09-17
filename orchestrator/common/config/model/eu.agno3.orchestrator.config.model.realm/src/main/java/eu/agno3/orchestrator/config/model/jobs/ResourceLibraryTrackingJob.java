/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class ResourceLibraryTrackingJob extends JobImpl {

    private StructuralObject anchor;
    private DateTime lastModified;


    /**
     * 
     */
    public ResourceLibraryTrackingJob () {
        super(new ConfigurationJobGroup());
    }


    /**
     * @param service
     */
    public void setAnchor ( StructuralObject service ) {
        this.anchor = service;
    }


    /**
     * @return the service
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @return the lastModified
     */
    public DateTime getLastModified () {
        return this.lastModified;
    }


    /**
     * @param lastModified
     *            the lastModified to set
     */
    public void setLastModified ( DateTime lastModified ) {
        this.lastModified = lastModified;
    }
}
