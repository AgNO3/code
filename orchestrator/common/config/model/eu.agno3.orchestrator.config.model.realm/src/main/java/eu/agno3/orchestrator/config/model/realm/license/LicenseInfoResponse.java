/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 14, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm.license;


import org.eclipse.jdt.annotation.NonNull;

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
public class LicenseInfoResponse extends XmlMarshallableMessage<@NonNull AgentMessageSource> implements ResponseMessage<@NonNull AgentMessageSource> {

    private LicenseInfo info;


    /**
     * 
     */
    public LicenseInfoResponse () {
        super();
    }


    /**
     * @param origin
     * @param ttl
     */
    public LicenseInfoResponse ( @NonNull AgentMessageSource origin, int ttl ) {
        super(origin, ttl);
    }


    /**
     * @param origin
     * @param replyTo
     */
    public LicenseInfoResponse ( @NonNull AgentMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public LicenseInfoResponse ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * @return the info
     */
    public LicenseInfo getInfo () {
        return this.info;
    }


    /**
     * @param info
     *            the info to set
     */
    public void setInfo ( LicenseInfo info ) {
        this.info = info;
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

}
