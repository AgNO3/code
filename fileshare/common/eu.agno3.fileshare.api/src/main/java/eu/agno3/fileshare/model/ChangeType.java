/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 23, 2016 by mbechler
 */
package eu.agno3.fileshare.model;


/**
 * @author mbechler
 *
 */
public enum ChangeType {

    /**
     * Child has been removed
     */
    REMOVAL,

    /**
     * Child has been moved
     */
    MOVE,

    /**
     * Child has been renamed
     */
    RENAME
}
