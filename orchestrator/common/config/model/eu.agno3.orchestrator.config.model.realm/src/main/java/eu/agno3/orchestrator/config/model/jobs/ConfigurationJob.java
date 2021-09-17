/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import java.util.UUID;

import eu.agno3.orchestrator.config.model.realm.ConfigApplyInfo;
import eu.agno3.orchestrator.config.model.realm.ServiceStructuralObject;
import eu.agno3.orchestrator.config.model.realm.StructuralObject;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 * 
 */
public class ConfigurationJob extends JobImpl {

    private ConfigApplyInfo applyInfo = new ConfigApplyInfo();
    private boolean noRestart;
    private boolean bootstrapping;
    private UUID instanceId;
    private StructuralObject anchor;
    private ServiceStructuralObject service;


    /**
     * 
     */
    public ConfigurationJob () {
        super(new ConfigurationJobGroup());
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.jobs.JobImpl#setJobGroup(eu.agno3.orchestrator.jobs.JobGroup)
     */
    @Override
    public void setJobGroup ( JobGroup jobGroup ) {
        // ignore
    }


    /**
     * @return the applyInfo
     */
    public ConfigApplyInfo getApplyInfo () {
        return this.applyInfo;
    }


    /**
     * @param applyInfo
     *            the applyInfo to set
     */
    public void setApplyInfo ( ConfigApplyInfo applyInfo ) {
        this.applyInfo = applyInfo;
    }


    /**
     * @param noRestart
     *            the noRestart to set
     */
    public void setNoRestart ( boolean noRestart ) {
        this.noRestart = noRestart;
    }


    /**
     * @return the noRestart
     */
    public boolean getNoRestart () {
        return this.noRestart;
    }


    /**
     * @param id
     */
    public void setInstanceId ( UUID id ) {
        this.instanceId = id;
    }


    /**
     * @return the instanceId
     */
    public UUID getInstanceId () {
        return this.instanceId;
    }


    /**
     * @return the bootstrapping
     */
    public boolean isBootstrapping () {
        return this.bootstrapping;
    }


    /**
     * @param bootstrapping
     *            the bootstrapping to set
     */
    public void setBootstrapping ( boolean bootstrapping ) {
        this.bootstrapping = bootstrapping;
    }


    /**
     * @return the service
     */
    public ServiceStructuralObject getService () {
        return this.service;
    }


    /**
     * @param service
     */
    public void setService ( ServiceStructuralObject service ) {
        this.service = service;
    }


    /**
     * @return the anchor
     */
    public StructuralObject getAnchor () {
        return this.anchor;
    }


    /**
     * @param anchor
     *            the anchor to set
     */
    public void setAnchor ( StructuralObject anchor ) {
        this.anchor = anchor;
    }

}
