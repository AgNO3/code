/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class ConfigApplyTrackingJob extends JobImpl {

    private StructuralObject anchor;


    /**
     * 
     */
    public ConfigApplyTrackingJob () {
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

}
