/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.07.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.server.util;


/**
 * @author mbechler
 * 
 */
public enum ValueTypes {

    /**
     * Values overriden by enforcement
     */
    ENFORCED,

    /**
     * Values that are local to the object
     */
    LOCAL,

    /**
     * Values that are inherited
     */
    INHERITED,

    /**
     * Values that are provided through defaults
     */
    DEFAULT

}
