/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.compound.CompoundJob;


/**
 * @author mbechler
 * 
 */
public class ConfigApplyJob extends CompoundJob {

    private Set<ServiceStructuralObject> services = new HashSet<>();


    /**
     * 
     */
    public ConfigApplyJob () {
        super();
        setJobGroup(new ConfigurationJobGroup());
        setJobs(new ArrayList<>());
    }


    /**
     * @param service
     */
    public void setServices ( Set<ServiceStructuralObject> service ) {
        this.services = service;
    }


    /**
     * @return the service
     */
    public Set<ServiceStructuralObject> getService () {
        return this.services;
    }

}
