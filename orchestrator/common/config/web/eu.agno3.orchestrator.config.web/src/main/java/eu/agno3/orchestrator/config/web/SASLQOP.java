/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.01.2017 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public enum SASLQOP {

    /**
     * authentication only
     */
    AUTH,

    /**
     * + transport integrity
     */
    INTEGRITY,

    /**
     * + confidentiality
     */
    CONF
}
