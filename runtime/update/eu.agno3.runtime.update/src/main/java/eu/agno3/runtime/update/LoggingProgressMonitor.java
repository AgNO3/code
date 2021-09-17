/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 16.09.2014 by mbechler
 */
package eu.agno3.runtime.update;


import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * @author mbechler
 * 
 */
public class LoggingProgressMonitor implements IProgressMonitor {

    private static final Logger log = Logger.getLogger(LoggingProgressMonitor.class);

    private int at;
    private int totalWork;
    private int lastAt;


    @Override
    public void worked ( int work ) {
        this.at += work;
        log.info("Worked " + 100.0f * ( (float) this.at / (float) this.totalWork )); //$NON-NLS-1$
    }


    @Override
    public void subTask ( String name ) {}


    @Override
    public void setTaskName ( String name ) {}


    @Override
    public void setCanceled ( boolean value ) {}


    @Override
    public boolean isCanceled () {
        return false;
    }


    @Override
    public void internalWorked ( double work ) {
        this.lastAt = this.at;
        this.at += work;
        if ( this.lastAt != this.at && log.isDebugEnabled() ) {
            log.debug(String.format("iWorked %.2f", 100.0f * ( (float) this.at / (float) this.totalWork ))); //$NON-NLS-1$
        }
    }


    @Override
    public void done () {
        log.debug("done"); //$NON-NLS-1$
        this.at = 0;
    }


    @Override
    public void beginTask ( String name, int tw ) {
        this.totalWork = tw;
        if ( log.isDebugEnabled() ) {
            log.debug("Starting " + name); //$NON-NLS-1$
        }
    }
}