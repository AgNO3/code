/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.01.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import eu.agno3.runtime.util.serialization.SafeSerialization;


/**
 * @author mbechler
 *
 */
@SafeSerialization
public enum SubjectType {

    /**
     * This is local subject
     */
    LOCAL,

    /**
     * This is a synchronized subject
     */
    REMOTE
}
