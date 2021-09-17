/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.10.2015 by mbechler
 */
package eu.agno3.fileshare.model;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface MappedVFSEntity extends VFSEntity {

    /**
     * @return the vfs
     */
    VirtualGroup getVfs ();


    /**
     * @param vfs
     *            the vfs to set
     */
    void setVfs ( VirtualGroup vfs );


    /**
     * @return the actual entity
     */
    VFSEntity getDelegate ();


    /**
     * @param delegate
     */
    void setDelegate ( VFSEntity delegate );


    /**
     * @return the reference storage
     */
    EntityReferenceStorage getReferenceStorage ();


    /**
     * @param refStorage
     */
    void setReferenceStorage ( EntityReferenceStorage refStorage );


    /**
     * @param inode
     */
    void setInode ( byte[] inode );


    /**
     * @return the persisted local valid grants
     */
    Set<Grant> getOriginalLocalValidGrants ();

}