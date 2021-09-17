/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: May 15, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import org.joda.time.DateTime;

import eu.agno3.orchestrator.config.model.base.config.ConfigurationState;


/**
 * @author mbechler
 *
 */
public class InstanceStatus {

    private ConfigurationState compositeConfigurationState = ConfigurationState.UNKNOWN;
    private DateTime lastUpdated;


    /**
     * @return the compositeConfigurationState
     */
    public ConfigurationState getCompositeConfigurationState () {
        return this.compositeConfigurationState;
    }


    /**
     * @param compositeConfigurationState
     *            the compositeConfigurationState to set
     */
    public void setCompositeConfigurationState ( ConfigurationState compositeConfigurationState ) {
        this.compositeConfigurationState = compositeConfigurationState;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString () {
        return this.compositeConfigurationState.name();
    }


    /**
     * @param lastUpdated
     */
    public void setLastUpdated ( DateTime lastUpdated ) {
        this.lastUpdated = lastUpdated;
    }


    /**
     * @return the lastUpdated
     */
    public DateTime getLastUpdated () {
        return this.lastUpdated;
    }
}
