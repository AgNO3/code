/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 27, 2017 by mbechler
 */
package eu.agno3.fileshare.service.aws.internal;


import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;

import com.amazonaws.SdkBaseException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.StorageClass;

import eu.agno3.fileshare.exceptions.InsufficentStorageSpaceException;
import eu.agno3.fileshare.exceptions.StorageException;
import eu.agno3.fileshare.model.VFSFileEntity;
import eu.agno3.fileshare.service.InputBuffer;
import eu.agno3.fileshare.service.api.internal.BlockStorageService;
import eu.agno3.fileshare.service.util.ServiceUtil;
import eu.agno3.fileshare.vfs.VFSContentHandle;
import eu.agno3.fileshare.vfs.VFSStoreHandle;


/**
 * @author mbechler
 *
 */
@Component ( service = BlockStorageService.class, configurationPid = "blockstore.aws", configurationPolicy = ConfigurationPolicy.REQUIRE )
public class S3BlockStorage implements BlockStorageService {

    private static final Logger log = Logger.getLogger(S3BlockStorage.class);

    private static final StorageClass STORAGE_CLASS = StorageClass.Standard;

    private S3ClientProvider clientProvider;


    @Reference
    protected synchronized void setClientProvider ( S3ClientProvider cp ) {
        this.clientProvider = cp;
    }


    protected synchronized void unsetClientProvider ( S3ClientProvider cp ) {
        if ( this.clientProvider == cp ) {
            this.clientProvider = null;
        }
    }


    @Override
    public VFSContentHandle getContents ( VFSFileEntity entity ) throws StorageException {
        if ( log.isDebugEnabled() ) {
            log.debug("Fetch " + entity); //$NON-NLS-1$
        }
        AmazonS3 cl = this.clientProvider.getClient();
        return new S3ContentHandle(cl, this.clientProvider.getBucket(), Hex.encodeHexString(entity.getInode()));
    }


    @Override
    public VFSStoreHandle storeContents ( VFSFileEntity f, InputBuffer data ) throws StorageException {
        if ( log.isDebugEnabled() ) {
            log.debug("Store " + f); //$NON-NLS-1$
            log.debug("Buffer is " + data); //$NON-NLS-1$
        }
        AmazonS3 cl = this.clientProvider.getClient();

        ObjectMetadata om = new ObjectMetadata();
        om.setContentLength(f.getFileSize());

        if ( f.getContentEncoding() != null ) {
            om.setContentEncoding(f.getContentEncoding());
        }

        om.setContentType(f.getContentType());
        om.setLastModified(f.getLastModified().toDate());
        om.setContentDisposition("attachment;" + //$NON-NLS-1$
                ServiceUtil.encodeDispositionFilename(f.getLocalName()));

        om.addUserMetadata("ownerId", f.getOwner().getId().toString()); //$NON-NLS-1$
        om.addUserMetadata("fileKey", f.getEntityKey().toString()); //$NON-NLS-1$
        String key = Hex.encodeHexString(f.getInode());

        try ( SeekableByteChannel ch = data.getStream();
              InputStream is = Channels.newInputStream(ch) ) {

            // unicode chars cause signature verification errors, as least in minio
            om.addUserMetadata(
                "filename", //$NON-NLS-1$
                URLEncoder.encode(f.getLocalName(), "UTF-8")); //$NON-NLS-1$

            PutObjectRequest pr = new PutObjectRequest(this.clientProvider.getBucket(), key, is, om);
            pr.setStorageClass(STORAGE_CLASS);
            cl.putObject(pr);
            return new S3VFSStoreHandle(cl, this.clientProvider.getBucket(), key, data.getSize());
        }
        catch (
            IOException |
            SdkBaseException e ) {
            throw wrapException("Failed to store file " + f, e); //$NON-NLS-1$
        }
    }


    /**
     * @param e
     * @return
     */
    private static StorageException wrapException ( String msg, Exception e ) {
        return new StorageException(msg, e);
    }


    @Override
    public VFSStoreHandle replaceContents ( VFSFileEntity f, InputBuffer data ) throws StorageException {
        return storeContents(f, data);
    }


    @Override
    public void removeContents ( VFSFileEntity f ) throws StorageException {
        if ( log.isDebugEnabled() ) {
            log.debug("Remove " + f); //$NON-NLS-1$
        }

        AmazonS3 cl = this.clientProvider.getClient();
        try {
            cl.deleteObject(this.clientProvider.getBucket(), Hex.encodeHexString(f.getInode()));
        }
        catch ( SdkBaseException e ) {
            throw wrapException("Failed to remove file " + f, e); //$NON-NLS-1$
        }
    }


    @Override
    public void checkFreeSpace ( long neededSize, long temporarySize ) throws InsufficentStorageSpaceException {
        // ignore for now
    }

}
