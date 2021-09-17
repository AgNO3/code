/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.resourcelibrary.ResourceLibrary;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class ResourceLibrarySynchronizationJob extends JobImpl {

    private ServiceStructuralObject service;
    private ResourceLibrary library;
    private String hint;
    private DateTime lastModified;


    /**
     * 
     */
    public ResourceLibrarySynchronizationJob () {
        super(new ConfigurationJobGroup());
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


    /**
     * @param service
     */
    public void setService ( ServiceStructuralObject service ) {
        this.service = service;
    }


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @return the library
     */
    public ResourceLibrary getLibrary () {
        return this.library;
    }


    /**
     * @param library
     *            the library to set
     */
    public void setLibrary ( ResourceLibrary library ) {
        this.library = library;
    }


    /**
     * @return the hint
     */
    public String getHint () {
        return this.hint;
    }


    /**
     * @param hint
     */
    public void setHint ( String hint ) {
        this.hint = hint;
    }
}
