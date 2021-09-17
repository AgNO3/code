/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 05.12.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.coord.output;


import eu.agno3.orchestrator.jobs.msg.JobOutputEvent;
import eu.agno3.orchestrator.jobs.msg.JobOutputLevel;


/**
 * @author mbechler
 *
 */
public final class OutputQueueEntry extends JobOutputEvent {

    private final boolean eof;
    private final String msg;


    /**
     * 
     */
    public OutputQueueEntry () {
        this.eof = true;
        this.msg = null;
    }


    /**
     * @param msg
     * @param level
     * @param levelPosition
     */
    public OutputQueueEntry ( String msg, JobOutputLevel level, long levelPosition ) {
        super(level, levelPosition);
        this.msg = msg;
        this.eof = false;
    }


    /**
     * @return the msg
     */
    public String getMsg () {
        return this.msg;
    }


    /**
     * @return the eof
     */
    @Override
    public boolean isEof () {
        return this.eof;
    }
}
