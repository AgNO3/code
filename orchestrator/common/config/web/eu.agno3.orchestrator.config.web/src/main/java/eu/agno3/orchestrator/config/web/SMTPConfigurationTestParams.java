/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.io.Serializable;

import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public class SMTPConfigurationTestParams implements ConfigTestParams, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4994482535186014750L;

    private String targetAddress;


    /**
     * @return the targetAddress
     */
    public String getTargetAddress () {
        return this.targetAddress;
    }


    /**
     * @param targetAddress
     *            the targetAddress to set
     */
    public void setTargetAddress ( String targetAddress ) {
        this.targetAddress = targetAddress;
    }
}
