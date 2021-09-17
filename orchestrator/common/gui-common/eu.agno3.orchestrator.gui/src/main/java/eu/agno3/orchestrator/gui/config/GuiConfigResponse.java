/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 23.09.2013 by mbechler
 */
package eu.agno3.orchestrator.gui.config;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.server.component.msg.ComponentConfigResponse;
import eu.agno3.orchestrator.server.messaging.addressing.ServerMessageSource;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;
import eu.agno3.runtime.messaging.msg.ResponseStatus;
import eu.agno3.runtime.messaging.xml.XmlMarshallableMessage;


/**
 * @author mbechler
 * 
 */
public class GuiConfigResponse extends XmlMarshallableMessage<@NonNull ServerMessageSource> implements ComponentConfigResponse<GuiConfig> {

    private GuiConfig guiConfiguration;


    /**
     * 
     */
    public GuiConfigResponse () {}


    /**
     * @param config
     * @param origin
     * @param replyTo
     */
    public GuiConfigResponse ( @NonNull GuiConfig config, @NonNull ServerMessageSource origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        super(origin, replyTo);
        this.guiConfiguration = config;
    }


    /**
     * @param config
     * @param origin
     */
    public GuiConfigResponse ( @NonNull GuiConfig config, @NonNull ServerMessageSource origin ) {
        super(origin);
        this.guiConfiguration = config;
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
     * @return the agentConfiguration
     */
    @Override
    public GuiConfig getConfiguration () {
        return this.guiConfiguration;
    }


    /**
     * @param guiConfiguration
     *            the agentConfiguration to set
     */
    protected void setConfiguration ( @NonNull GuiConfig guiConfiguration ) {
        this.guiConfiguration = guiConfiguration;
    }

}
