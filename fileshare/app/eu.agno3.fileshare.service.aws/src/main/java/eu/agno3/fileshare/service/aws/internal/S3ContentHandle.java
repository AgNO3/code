/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.aws.internal;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import eu.agno3.fileshare.exceptions.FileshareException;
import eu.agno3.fileshare.vfs.VFSContentHandle;


/**
 * @author mbechler
 *
 */
public class S3ContentHandle implements VFSContentHandle {

    private final AmazonS3 cl;
    private final String bucket;
    private final String key;


    /**
     * @param cl
     * @param bucket
     * @param key
     */
    public S3ContentHandle ( AmazonS3 cl, String bucket, String key ) {
        this.cl = cl;
        this.bucket = bucket;
        this.key = key;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#close()
     */
    @Override
    public void close () throws FileshareException {
        // TODO Auto-generated method stub

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#haveStoredSize()
     */
    @Override
    public boolean haveStoredSize () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#getStoredSize()
     */
    @Override
    public long getStoredSize () throws FileshareException {
        return -1;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, byte[])
     */
    @Override
    public long transferTo ( OutputStream out, byte[] buffer ) throws IOException {
        GetObjectRequest gr = new GetObjectRequest(this.bucket, this.key);
        S3Object s3 = this.cl.getObject(gr);
        try ( S3ObjectInputStream is = s3.getObjectContent() ) {
            return IOUtils.copyLarge(is, out, buffer);
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.vfs.VFSContentHandle#transferTo(java.io.OutputStream, long, long, byte[])
     */
    @Override
    public long transferTo ( OutputStream out, long start, long length, byte[] buffer ) throws IOException {
        GetObjectRequest gr = new GetObjectRequest(this.bucket, this.key);
        gr.setRange(start, start + length - 1);
        S3Object s3 = this.cl.getObject(gr);
        try ( S3ObjectInputStream is = s3.getObjectContent() ) {
            return IOUtils.copyLarge(is, out, buffer);
        }
    }

}
