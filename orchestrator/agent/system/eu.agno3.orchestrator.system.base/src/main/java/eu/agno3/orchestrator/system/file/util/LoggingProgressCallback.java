/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.file.util;


import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class LoggingProgressCallback implements ProgressCallback {

    private static final Logger log = Logger.getLogger(LoggingProgressCallback.class);
    private long totalSize;


    @Override
    public void start ( long size ) {
        if ( log.isDebugEnabled() ) {
            log.debug("START " + size); //$NON-NLS-1$
        }
        this.totalSize = size;
    }


    @Override
    public void progress ( long pos ) {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("At %.1f%%", 100 * (double) pos / this.totalSize)); //$NON-NLS-1$
        }
    }


    @Override
    public void finished () {
        log.debug("DONE"); //$NON-NLS-1$
    }
}