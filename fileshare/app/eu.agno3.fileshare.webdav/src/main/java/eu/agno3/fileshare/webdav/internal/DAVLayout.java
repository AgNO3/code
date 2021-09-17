/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Apr 22, 2016 by mbechler
 */
package eu.agno3.fileshare.webdav.internal;


/**
 * @author mbechler
 *
 */
public enum DAVLayout {

    /**
     * Regular layout for producing readable names when clients don't honor display names
     */
    REGULAR,

    /**
     * Layout with user root at the top, owncloud compatible properties and chunking upload
     */
    OWNCLOUD,

    /**
     * Native layout producing absolutely consistent paths
     */
    NATIVE
}
