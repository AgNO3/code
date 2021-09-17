/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 06.05.2014 by mbechler
 */
package eu.agno3.orchestrator.jobs.msg;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.jobs.JobInfo;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class JobInfoReplyMessage extends XmlMarshallableMessage<@NonNull MessageSource> implements ResponseMessage<@NonNull MessageSource> {

    private JobInfo jobInfo;


    /**
     * 
     */
    public JobInfoReplyMessage () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public JobInfoReplyMessage ( @NonNull MessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public JobInfoReplyMessage ( @NonNull MessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public JobInfoReplyMessage ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.ResponseMessage#getStatus()
     */
    @Override
    public ResponseStatus getStatus () {
        return ResponseStatus.SUCCESS;
    }


    /**
     * @return the info
     */
    public JobInfo getJobInfo () {
        return this.jobInfo;
    }


    /**
     * @param info
     *            the info to set
     */
    public void setJobInfo ( JobInfo info ) {
        this.jobInfo = info;
    }

}
