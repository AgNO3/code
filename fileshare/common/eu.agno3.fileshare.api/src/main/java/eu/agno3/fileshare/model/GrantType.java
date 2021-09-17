/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.02.2015 by mbechler
 */
package eu.agno3.fileshare.model;


/**
 * @author mbechler
 *
 */
public enum GrantType {

    /**
     * Shared to a user/group
     */
    SUBJECT,

    /**
     * Shared by mail
     */
    MAIL,

    /**
     * Shared by link
     */
    LINK
}
