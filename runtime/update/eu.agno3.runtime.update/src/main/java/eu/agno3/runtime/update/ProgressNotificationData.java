/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2015 by mbechler
 */
package eu.agno3.runtime.update;


import java.io.Serializable;


/**
 * @author mbechler
 *
 */
public class ProgressNotificationData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1056289630984726616L;
    private int at;
    private String task;
    private int totalWork;
    private float percent;
    /**
     * 
     */
    public static final String UPDATE_PROGRESS_TYPE = "update-progress"; //$NON-NLS-1$


    /**
     * 
     */
    public ProgressNotificationData () {}


    /**
     * @param at
     * @param totalWork
     * @param task
     * @param percent
     */
    public ProgressNotificationData ( int at, int totalWork, String task, float percent ) {
        this.at = at;
        this.totalWork = totalWork;
        this.task = task;
        this.percent = percent;
    }


    /**
     * @return the at
     */
    public int getAt () {
        return this.at;
    }


    /**
     * @return the task
     */
    public String getTask () {
        return this.task;
    }


    /**
     * @return the totalWork
     */
    public int getTotalWork () {
        return this.totalWork;
    }


    /**
     * @return the percent
     */
    public float getPercent () {
        return this.percent;
    }

}
