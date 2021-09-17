/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 4, 2017 by mbechler
 */
package eu.agno3.fileshare.service.chunks.internal;


import java.io.IOException;

import org.apache.log4j.Logger;

import eu.agno3.fileshare.service.UploadState;
import eu.agno3.fileshare.service.UploadStateTracker;


/**
 * @author mbechler
 *
 */
public abstract class BaseUploadStateTrackerImpl implements UploadStateTracker {

    private static final Logger log = Logger.getLogger(FileUploadStateTrackerImpl.class);
    private static final String UPLOADING_FLAG = ".uploading"; //$NON-NLS-1$
    private static final String FAILED_FLAG = ".failed"; //$NON-NLS-1$
    private static final String PROCESSING_FLAG = ".processing"; //$NON-NLS-1$
    private static final String COMPLETE_FLAG = ".complete"; //$NON-NLS-1$


    /**
     * 
     */
    public BaseUploadStateTrackerImpl () {
        super();
    }


    protected abstract void removeFlag ( String flag ) throws IOException;


    protected abstract void setFlag ( String flag ) throws IOException;


    protected abstract boolean checkFlag ( String flag );


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.fileshare.service.UploadStateTracker#getState()
     */
    @Override
    public UploadState getState () {
        if ( checkFlag(PROCESSING_FLAG) ) {
            return UploadState.PROCESSING;
        }
        if ( checkFlag(COMPLETE_FLAG) ) {
            return UploadState.COMPLETE;
        }
        else if ( checkFlag(FAILED_FLAG) ) {
            return UploadState.FAILED;
        }
        else if ( checkFlag(UPLOADING_FLAG) ) {
            return UploadState.UPLOADING;
        }
        return UploadState.INITIAL;
    }


    /**
     * @param state
     */
    @Override
    public void setState ( UploadState state ) {
        if ( !isValid() ) {
            log.debug("Context is gone"); //$NON-NLS-1$
            return;
        }
        if ( log.isTraceEnabled() ) {
            log.trace("Set state " + state); //$NON-NLS-1$
        }
        try {
            switch ( state ) {
            case COMPLETE:
                setFlag(COMPLETE_FLAG);
                removeFlag(PROCESSING_FLAG);
                break;
            case PROCESSING:
                setFlag(COMPLETE_FLAG);
                setFlag(PROCESSING_FLAG);
                break;
            case FAILED:
                setFlag(FAILED_FLAG);
                removeFlag(PROCESSING_FLAG);
                removeFlag(COMPLETE_FLAG);
                break;
            case UPLOADING:
                setFlag(UPLOADING_FLAG);
                removeFlag(FAILED_FLAG);
                break;
            default:
                break;
            }
        }
        catch ( IOException e ) {
            log.warn("Failed to set context state", e); //$NON-NLS-1$
        }
    }

}