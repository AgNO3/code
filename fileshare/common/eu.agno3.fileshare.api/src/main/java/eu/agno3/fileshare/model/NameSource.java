/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.05.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public enum NameSource {

    /**
     * 
     */
    UNKNOWN,

    /**
     * 
     */
    GROUP_NAME,

    /**
     * 
     */
    FULL_NAME,

    /**
     * 
     */
    MAIL,

    /**
     * 
     */
    USERNAME
}
