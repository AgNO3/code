/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.exec;


import eu.agno3.orchestrator.jobs.JobProgressInfo;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 * 
 */
public interface JobOutputHandler {

    /**
     * Start output handling
     */
    void start ();


    /**
     * Stop output handling
     */
    void end ();


    /**
     * @param jobProgressInfo
     */
    void setProgress ( JobProgressInfo jobProgressInfo );


    /**
     * 
     * @param msg
     */
    void logLineInfo ( String msg );


    /**
     * @param msg
     * @param t
     */
    void logLineInfo ( String msg, Throwable t );


    /**
     * 
     * @param msg
     */
    void logLineWarn ( String msg );


    /**
     * 
     * @param msg
     * @param t
     */
    void logLineWarn ( String msg, Throwable t );


    /**
     * 
     * @param msg
     */
    void logLineError ( String msg );


    /**
     * 
     * @param msg
     * @param t
     */
    void logLineError ( String msg, Throwable t );


    /**
     * @param l
     * @param buffer
     */
    void logBuffer ( JobOutputLevel l, String buffer );


    /**
     * 
     */
    void eof ();

}
