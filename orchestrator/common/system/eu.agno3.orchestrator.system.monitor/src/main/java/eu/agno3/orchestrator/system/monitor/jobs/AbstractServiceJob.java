/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 14.03.2016 by mbechler
 */
package eu.agno3.orchestrator.system.monitor.jobs;


import java.io.Serializable;

import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 *
 */
public class AbstractServiceJob extends JobImpl implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6326250689884633282L;

    private ServiceStructuralObject service;


    /**
     * 
     */
    public AbstractServiceJob () {
        super(new ServiceJobGroup());
    }


    /**
     * @param service
     */
    public AbstractServiceJob ( ServiceStructuralObject service ) {
        this();
        this.service = service;
    }


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @param service
     *            the service to set
     */
    public void setService ( ServiceStructuralObject service ) {
        this.service = service;
    }

}