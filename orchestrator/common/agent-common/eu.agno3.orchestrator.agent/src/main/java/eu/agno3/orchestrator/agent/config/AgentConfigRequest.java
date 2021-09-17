/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.config;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.agent.msg.addressing.AgentMessageSource;
import eu.agno3.orchestrator.server.component.msg.ComponentConfigRequest;
import eu.agno3.runtime.messaging.addressing.DefaultMessageTarget;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.addressing.MessageTarget;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.xml.DefaultXmlErrorResponseMessage;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class AgentConfigRequest extends XmlMarshallableMessage<@NonNull AgentMessageSource>
        implements ComponentConfigRequest<@NonNull AgentMessageSource, AgentConfig, AgentConfigResponse, DefaultXmlErrorResponseMessage> {

    /**
     * 
     */
    private static final int AGENT_CONFIG_REQUEST_TIMEOUT = 5000;

    private String imageType;
    private String hostAddress;
    private String hostName;
    private Long buildVersion;


    /**
     * 
     */
    public AgentConfigRequest () {}


    /**
     * @param origin
     * @param replyTo
     */
    public AgentConfigRequest ( @NonNull AgentMessageSource origin, Message<@NonNull MessageSource> replyTo ) {
        super(origin, replyTo);
    }


    /**
     * @param origin
     */
    public AgentConfigRequest ( @NonNull AgentMessageSource origin ) {
        super(origin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getResponseType()
     */
    @Override
    public Class<AgentConfigResponse> getResponseType () {
        return AgentConfigResponse.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getErrorResponseType()
     */
    @Override
    public Class<DefaultXmlErrorResponseMessage> getErrorResponseType () {
        return DefaultXmlErrorResponseMessage.class;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getTarget()
     */
    @Override
    public MessageTarget getTarget () {
        return new DefaultMessageTarget();
    }


    /**
     * @return the hostAddress
     */
    public String getHostAddress () {
        return this.hostAddress;
    }


    /**
     * @param hostAddress
     *            the hostAddress to set
     */
    public void setHostAddress ( String hostAddress ) {
        this.hostAddress = hostAddress;
    }


    /**
     * @return the hostName
     */
    public String getHostName () {
        return this.hostName;
    }


    /**
     * @param hostName
     *            the hostName to set
     */
    public void setHostName ( String hostName ) {
        this.hostName = hostName;
    }


    /**
     * @return the imageType
     */
    public String getImageType () {
        return this.imageType;
    }


    /**
     * @param imageType
     *            the imageType to set
     */
    public void setImageType ( String imageType ) {
        this.imageType = imageType;
    }


    /**
     * @return the buildVersion
     */
    public Long getBuildVersion () {
        return this.buildVersion;
    }


    /**
     * @param applianceBuild
     */
    public void setBuildVersion ( Long applianceBuild ) {
        this.buildVersion = applianceBuild;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.messaging.msg.RequestMessage#getReplyTimeout()
     */
    @Override
    public long getReplyTimeout () {
        return AGENT_CONFIG_REQUEST_TIMEOUT;
    }

}
