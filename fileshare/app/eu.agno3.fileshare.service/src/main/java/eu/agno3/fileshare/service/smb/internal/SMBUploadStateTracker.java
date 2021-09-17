/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.smb.internal;


import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.service.chunks.internal.BaseUploadStateTrackerImpl;

import jcifs.CIFSException;
import jcifs.SmbResource;


/**
 * @author mbechler
 *
 */
public class SMBUploadStateTracker extends BaseUploadStateTrackerImpl {

    private static final Logger log = Logger.getLogger(SMBUploadStateTracker.class);

    private SmbResource context;


    /**
     * @param ctxp
     */
    public SMBUploadStateTracker ( SmbResource ctxp ) {
        this.context = ctxp;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.UploadStateTracker#isValid()
     */
    @Override
    public boolean isValid () {
        try {
            return this.context.exists();
        }
        catch ( CIFSException e ) {
            log.warn("Failed to check validity", e); //$NON-NLS-1$
            return false;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseUploadStateTrackerImpl#removeFlag(java.lang.String)
     */
    @Override
    protected void removeFlag ( String flag ) throws IOException {
        try ( SmbResource f = this.context.resolve(flagName(flag)) ) {
            if ( f.exists() ) {
                f.delete();
            }
        }
        catch ( CIFSException e ) {
            log.warn("Failed to remove flag", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseUploadStateTrackerImpl#setFlag(java.lang.String)
     */
    @Override
    protected void setFlag ( String flag ) throws IOException {
        try ( SmbResource f = this.context.resolve(flagName(flag)) ) {
            try ( OutputStream oos = f.openOutputStream() ) {}
        }
        catch ( CIFSException e ) {
            log.warn("Failed to set flag", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.chunks.internal.BaseUploadStateTrackerImpl#checkFlag(java.lang.String)
     */
    @Override
    protected boolean checkFlag ( String flag ) {
        try ( SmbResource f = this.context.resolve(flagName(flag)) ) {
            return f.exists();
        }
        catch ( CIFSException e ) {
            log.warn("Failed to check flag", e); //$NON-NLS-1$
            return false;
        }
    }


    private static String flagName ( String flag ) {
        return flag;
    }

}
