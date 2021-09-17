/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 27.04.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.base.config;


import javax.xml.bind.annotation.XmlEnum;


/**
 * @author mbechler
 * 
 */
@XmlEnum
public enum ConfigurationState {

    /**
     * 
     */
    UNKNOWN,

    /**
     * No configuration available
     */
    UNCONFIGURED,

    /**
     * A new configuration is available
     */
    UPDATE_AVAILABLE,

    /**
     * The default values for this configuration have changed
     */
    DEFAULTS_CHANGED,

    /**
     * Application of this configuration is currently in progress
     */
    APPLYING,

    /**
     * The configuration is applied
     */
    APPLIED,

    /**
     * Applying the configuration failed
     */
    FAILED,
}
