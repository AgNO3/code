/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.03.2016 by mbechler
 */
package eu.agno3.orchestrator.jobs.agent.backup.internal;


import java.util.Optional;

import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.jobs.agent.backup.BackupException;
import eu.agno3.orchestrator.jobs.agent.backup.BackupManager;
import eu.agno3.orchestrator.system.backups.msg.BackupListRequest;
import eu.agno3.orchestrator.system.backups.msg.BackupListResponse;
import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = RequestEndpoint.class, property = "msgType=eu.agno3.orchestrator.system.backups.msg.BackupListRequest" )
public class BackupListRequestEndpoint implements RequestEndpoint<BackupListRequest, BackupListResponse, DefaultXmlErrorResponseMessage> {

    private BackupManager backupManager;
    private Optional<@NonNull AgentMessageSource> messageSource = Optional.empty();


    @Reference
    protected synchronized void setMessageSource ( @NonNull MessageSource ms ) {
        this.messageSource = Optional.of((AgentMessageSource) ms);
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.messageSource.equals(ms) ) {
            this.messageSource = Optional.empty();
        }
    }


    @Reference
    protected synchronized void setBackupManager ( BackupManager bm ) {
        this.backupManager = bm;
    }


    protected synchronized void unsetBackupManager ( BackupManager bm ) {
        if ( this.backupManager == bm ) {
            this.backupManager = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public BackupListResponse onReceive ( @NonNull BackupListRequest msg ) throws MessageProcessingException, MessagingException {

        try {
            BackupListResponse resp = new BackupListResponse(this.messageSource.get(), msg);
            resp.setBackups(this.backupManager.list());
            return resp;
        }
        catch ( BackupException e ) {
            throw new MessageProcessingException(new DefaultXmlErrorResponseMessage(e, this.messageSource.get(), msg));
        }

    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#getMessageType()
     */
    @Override
    public Class<BackupListRequest> getMessageType () {
        return BackupListRequest.class;
    }

}
