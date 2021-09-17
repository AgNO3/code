/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


/**
 * @author mbechler
 *
 */
public enum LDAPAuthType {

    /**
     * Anonymous bind
     */
    ANONYMOUS,

    /**
     * Authenticate via simple bind
     */
    SIMPLE,

    /**
     * Authenticate via SASL
     */
    SASL
}
