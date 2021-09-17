/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.12.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import org.apache.log4j.Logger;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;


/**
 * @author mbechler
 *
 */
public class PermissionFixupJobChangeListener implements IJobChangeListener {

    private static final Logger log = Logger.getLogger(PermissionFixupJobChangeListener.class);


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#done(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void done ( IJobChangeEvent ev ) {
        log.info("Provisioning job finished " + ev.getResult()); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#aboutToRun(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void aboutToRun ( IJobChangeEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#awake(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void awake ( IJobChangeEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#running(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void running ( IJobChangeEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#scheduled(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void scheduled ( IJobChangeEvent ev ) {
        // ignore
    }


    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.core.runtime.jobs.IJobChangeListener#sleeping(org.eclipse.core.runtime.jobs.IJobChangeEvent)
     */
    @Override
    public void sleeping ( IJobChangeEvent ev ) {
        // ignore
    }

}
