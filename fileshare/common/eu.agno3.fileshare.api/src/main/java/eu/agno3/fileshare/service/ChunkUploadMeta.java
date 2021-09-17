/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service;


import eu.agno3.fileshare.model.EntityKey;


/**
 * @author mbechler
 *
 */
public interface ChunkUploadMeta {

    /**
     * 
     * whether this is a context not created by native clients (e.g. created by owncloud client)
     * 
     * @return upload initiated by external source
     */
    boolean isExternalSource ();


    /**
     * 
     * @return upload target
     */
    EntityKey getTarget ();


    /**
     * 
     * @return whether the target is a file (to replace)
     */
    boolean isTargetFile ();


    /**
     * 
     * @return replace operation
     */
    boolean isReplacing ();


    /**
     * 
     * @return file name
     */
    String getLocalName ();


    /**
     * 
     * @return file content type
     */
    String getContentType ();


    /**
     * 
     * @return upload reference id
     */
    String getReference ();


    /**
     * @return total file size
     */
    Long getTotalSize ();


    /**
     * @return used chunk size
     */
    Long getChunkSize ();

}