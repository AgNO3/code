/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.aws.internal;


import com.amazonaws.SdkBaseException;
import com.amazonaws.services.s3.AmazonS3;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.vfs.VFSStoreHandle;


/**
 * @author mbechler
 *
 */
public class S3VFSStoreHandle implements VFSStoreHandle {

    private long size;
    private AmazonS3 client;
    private String bucket;
    private String id;


    /**
     * @param client
     * @param bucket
     * @param id
     * @param size
     * 
     */
    public S3VFSStoreHandle ( AmazonS3 client, String bucket, String id, long size ) {
        this.client = client;
        this.bucket = bucket;
        this.id = id;
        this.size = size;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#getLength()
     */
    @Override
    public long getLength () {
        return this.size;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#revert()
     */
    @Override
    public void revert () throws FileshareException {
        try {
            this.client.deleteObject(this.bucket, this.id);
        }
        catch ( SdkBaseException e ) {
            throw new StorageException("Failed to revert S3 file store on " + this.bucket + ':' + this.id, e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSStoreHandle#commit()
     */
    @Override
    public void commit () {

    }
}
