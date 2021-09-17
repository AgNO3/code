/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.10.2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.msg;


import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;


/**
 * @author mbechler
 *
 */
public class AgentServiceEntry {

    private ServiceStructuralObject service;
    private Long appliedRevision;
    private Long failsafeRevision;


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


    /**
     * @return the appliedRevision
     */
    public Long getAppliedRevision () {
        return this.appliedRevision;
    }


    /**
     * @param appliedRevision
     *            the appliedRevision to set
     */
    public void setAppliedRevision ( Long appliedRevision ) {
        this.appliedRevision = appliedRevision;
    }


    /**
     * @return the failsafeRevision
     */
    public Long getFailsafeRevision () {
        return this.failsafeRevision;
    }


    /**
     * @param failsafeRevision
     *            the failsafeRevision to set
     */
    public void setFailsafeRevision ( Long failsafeRevision ) {
        this.failsafeRevision = failsafeRevision;
    }
}
