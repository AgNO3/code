/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.07.2015 by mbechler
 */
package org.primefaces.fixed.component.fileupload;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.NotImplementedException;
import org.primefaces.model.UploadedFile;


/**
 * @author mbechler
 *
 */
public class HttpUploadedFile implements UploadedFile, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -9162063619724392770L;
    private long size;
    private transient InputStream is;
    private String contentType;
    private String fileName;


    /**
     * @param request
     * @param fileName
     * @throws IOException
     */
    public HttpUploadedFile ( HttpServletRequest request, String fileName ) throws IOException {
        this.fileName = fileName;
        this.size = request.getContentLengthLong();
        this.is = request.getInputStream();
        this.contentType = request.getContentType();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#getFileName()
     */
    @Override
    public String getFileName () {
        return this.fileName;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#getInputstream()
     */
    @Override
    public InputStream getInputstream () throws IOException {
        if ( this.is == null ) {
            throw new IOException("No inputstream available"); //$NON-NLS-1$
        }
        return this.is;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#getSize()
     */
    @Override
    public long getSize () {
        return this.size;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#getContents()
     */
    @Override
    public byte[] getContents () {
        throw new NotImplementedException("getContents is not implemented"); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#getContentType()
     */
    @Override
    public String getContentType () {
        return this.contentType;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.primefaces.model.UploadedFile#write(java.lang.String)
     */
    @Override
    public void write ( String filePath ) throws Exception {
        throw new NotImplementedException("write() is not implemented"); //$NON-NLS-1$
    }

}
