/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.jobs;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResultImpl;
import eu.agno3.orchestrator.jobs.JobGroup;
import eu.agno3.orchestrator.jobs.JobImpl;


/**
 * @author mbechler
 * 
 */
public class ConfigurationTestJob extends JobImpl {

    private ConfigTestParams parameters;
    private ConfigurationObject effectiveConfig;
    private ConfigTestResultImpl initialResult;


    /**
     * 
     */
    public ConfigurationTestJob () {
        super(new ConfigurationTestJobGroup());
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
     * @return the parameters
     */
    public ConfigTestParams getParameters () {
        return this.parameters;
    }


    /**
     * @param parameters
     *            the parameters to set
     */
    public void setParameters ( ConfigTestParams parameters ) {
        this.parameters = parameters;
    }


    /**
     * @return the effectiveConfig
     */
    public ConfigurationObject getEffectiveConfig () {
        return this.effectiveConfig;
    }


    /**
     * @param effectiveConfig
     *            the effectiveConfig to set
     */
    public void setEffectiveConfig ( ConfigurationObject effectiveConfig ) {
        this.effectiveConfig = effectiveConfig;
    }


    /**
     * @return the initialResult
     */
    public ConfigTestResultImpl getInitialResult () {
        return this.initialResult;
    }


    /**
     * @param initialResult
     *            the initialResult to set
     */
    public void setInitialResult ( ConfigTestResultImpl initialResult ) {
        this.initialResult = initialResult;
    }
}
