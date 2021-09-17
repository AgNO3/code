/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.runtime.update.internal;


import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;

import org.eclipse.core.runtime.IProgressMonitor;

import eu.agno3.runtime.update.ProgressNotificationData;


/**
 * @author mbechler
 *
 */
public class JMXProgressMonitor implements IProgressMonitor {

    private static final double DELTA = 0.1;

    private long sequence;
    private int at;
    private int totalWork;
    private int lastAt;

    private NotificationBroadcasterSupport emitter;

    private String task;


    /**
     * @param emitter
     */
    public JMXProgressMonitor ( NotificationBroadcasterSupport emitter ) {
        this.emitter = emitter;
    }


    @Override
    public void worked ( int work ) {
        this.at += work;
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

        if ( this.at - this.lastAt > DELTA ) {
            this.emitter.sendNotification(makeNotification());
        }

    }


    /**
     * @return
     */
    private Notification makeNotification () {
        Notification not = new Notification(ProgressNotificationData.UPDATE_PROGRESS_TYPE, this.emitter, this.sequence++);
        float percent = 100.0f * ( (float) this.at / (float) this.totalWork );
        not.setUserData(new ProgressNotificationData(this.at, this.totalWork, this.task, percent));
        return not;
    }


    @Override
    public void done () {
        this.at = 0;
    }


    @Override
    public void beginTask ( String name, int tw ) {
        this.totalWork = tw;
        this.task = name;
    }

}
