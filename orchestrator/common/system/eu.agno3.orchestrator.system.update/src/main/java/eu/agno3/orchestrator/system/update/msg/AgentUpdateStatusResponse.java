/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.update.msg;


import org.eclipse.jdt.annotation.NonNull;
import org.joda.time.DateTime;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class AgentUpdateStatusResponse extends XmlMarshallableMessage<@NonNull AgentMessageSource>
        implements ResponseMessage<@NonNull AgentMessageSource> {

    private Long currentSequence;
    private String currentStream;
    private DateTime revertTimestamp;
    private Long revertSequence;
    private String revertStream;
    private DateTime currentInstallDate;
    private boolean wantReboot;


    /**
     * 
     */
    public AgentUpdateStatusResponse () {
        super();
    }


    /**
     * 
     * @param origin
     * @param ttl
     */
    public AgentUpdateStatusResponse ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * 
     * @param origin
     * @param replyTo
     */
    public AgentUpdateStatusResponse ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * 
     * @param origin
     */
    public AgentUpdateStatusResponse ( @NonNull AgentMessageSource origin ) {
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
     * @return the current update sequence number
     */
    public Long getCurrentSequence () {
        return this.currentSequence;
    }


    /**
     * @param currentSequence
     *            the currentSequence to set
     */
    public void setCurrentSequence ( Long currentSequence ) {
        this.currentSequence = currentSequence;
    }


    /**
     * @return the currentStream
     */
    public String getCurrentStream () {
        return this.currentStream;
    }


    /**
     * @param currentStream
     *            the currentStream to set
     */
    public void setCurrentStream ( String currentStream ) {
        this.currentStream = currentStream;
    }


    /**
     * @return the timestamp that the revertible update was installed
     */
    public DateTime getRevertTimestamp () {
        return this.revertTimestamp;
    }


    /**
     * @param revertTimestamp
     *            the revertTimestamp to set
     */
    public void setRevertTimestamp ( DateTime revertTimestamp ) {
        this.revertTimestamp = revertTimestamp;
    }


    /**
     * @return the sequence of the revertible update
     */
    public Long getRevertSequence () {
        return this.revertSequence;
    }


    /**
     * @param revertSequence
     *            the revertSequence to set
     */
    public void setRevertSequence ( Long revertSequence ) {
        this.revertSequence = revertSequence;
    }


    /**
     * @return the stream of the revertible update
     */
    public String getRevertStream () {
        return this.revertStream;
    }


    /**
     * @param revertStream
     *            the revertStream to set
     */
    public void setRevertStream ( String revertStream ) {
        this.revertStream = revertStream;
    }


    /**
     * @return the currentInstallDate
     */
    public DateTime getCurrentInstallDate () {
        return this.currentInstallDate;
    }


    /**
     * @param installDate
     */
    public void setCurrentInstallDate ( DateTime installDate ) {
        this.currentInstallDate = installDate;
    }


    /**
     * @return the wantReboot
     */
    public boolean getRebootIndicated () {
        return this.wantReboot;
    }


    /**
     * @param wantReboot
     *            the wantReboot to set
     */
    public void setRebootIndicated ( boolean wantReboot ) {
        this.wantReboot = wantReboot;
    }

}
