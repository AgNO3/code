/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.orchestrator.agent.update.internal;


import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;

import eu.agno3.runtime.update.ProgressNotificationData;


/**
 * @author mbechler
 *
 */
public class JMXUpdateManagerNotificationListener implements NotificationListener {

    private static final Logger log = Logger.getLogger(JMXUpdateManagerNotificationListener.class);
    private IProgressMonitor monitor;

    private boolean started;
    private int at;


    /**
     * @param monitor
     */
    public JMXUpdateManagerNotificationListener ( IProgressMonitor monitor ) {
        this.monitor = monitor;
    }


    /**
     * {@inheritDoc}
     *
     * @see javax.management.NotificationListener#handleNotification(javax.management.Notification, java.lang.Object)
     */
    @Override
    public void handleNotification ( Notification notification, Object handback ) {

        if ( !ProgressNotificationData.UPDATE_PROGRESS_TYPE.equals(notification.getType())
                || ! ( notification.getUserData() instanceof ProgressNotificationData ) ) {
            log.debug("Invalid event"); //$NON-NLS-1$
            return;
        }

        ProgressNotificationData d = (ProgressNotificationData) notification.getUserData();
        log.debug("Got progress " + d.getPercent()); //$NON-NLS-1$

        if ( !this.started ) {
            log.debug(String.format("Begin task %s with %d", d.getTask(), d.getTotalWork())); //$NON-NLS-1$
            this.started = true;
            this.monitor.beginTask(d.getTask(), d.getTotalWork());
        }

        int worked = d.getAt() - this.at;
        log.trace("got work " + worked); //$NON-NLS-1$
        this.monitor.worked(worked);
        this.at = d.getAt();
    }

}
