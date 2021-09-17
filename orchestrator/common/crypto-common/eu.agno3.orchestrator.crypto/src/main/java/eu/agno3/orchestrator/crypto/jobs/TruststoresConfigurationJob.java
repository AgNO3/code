/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.05.2014 by mbechler
 */
package eu.agno3.orchestrator.crypto.jobs;


import eu.agno3.orchestrator.config.crypto.truststore.TruststoresConfig;
import eu.agno3.orchestrator.config.model.jobs.ConfigurationJob;


/**
 * @author mbechler
 * 
 */
public class TruststoresConfigurationJob extends ConfigurationJob {

    private TruststoresConfig tsConfig;


    /**
     * @return the tsConfig
     */
    public TruststoresConfig getTsConfig () {
        return this.tsConfig;
    }


    /**
     * @param tsConfig
     *            the tsConfig to set
     */
    public void setTsConfig ( TruststoresConfig tsConfig ) {
        this.tsConfig = tsConfig;
    }
}
