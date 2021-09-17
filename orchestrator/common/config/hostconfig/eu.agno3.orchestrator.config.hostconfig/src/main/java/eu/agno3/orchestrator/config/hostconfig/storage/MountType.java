/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


/**
 * @author mbechler
 *
 */
public enum MountType {

    /**
     * Local (ext4 filesystem)
     */
    LOCAL,

    /**
     * CIFS
     */
    CIFS,

    /**
     * NFS
     */
    NFS
}
