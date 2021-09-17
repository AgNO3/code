/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2015 by mbechler
 */
package eu.agno3.orchestrator.system.backups.msg;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.system.backups.BackupInfo;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseMessage;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 *
 */
public class BackupListResponse extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements ResponseMessage<@NonNull AgentMessageSource> {

    private List<BackupInfo> backups = new ArrayList<>();


    /**
     * 
     */
    public BackupListResponse () {
        super();
    }


    /**
     * 
     * @param origin
     * @param ttl
     */
    public BackupListResponse ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * 
     * @param origin
     * @param replyTo
     */
    public BackupListResponse ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * 
     * @param origin
     */
    public BackupListResponse ( @NonNull AgentMessageSource origin ) {
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
     * @return the backups
     */
    public List<BackupInfo> getBackups () {
        return this.backups;
    }


    /**
     * @param backups
     *            the backups to set
     */
    public void setBackups ( List<BackupInfo> backups ) {
        this.backups = backups;
    }

}
