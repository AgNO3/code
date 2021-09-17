/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Jun 13, 2017 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


/**
 * @author mbechler
 *
 */
public enum ADJoinType {

    /**
     * Join using an admisitrative account
     */
    ADMIN,

    /**
     * Join with a join account specified in the configuration
     */
    JOIN_ACCOUNT,

    /**
     * Join with a reset machine password
     */
    RESET_MACHINE_PASSWORD,

    /**
     * Join with a known machine password
     */
    CUSTOM_MACHINE_PASSWORD,

}
