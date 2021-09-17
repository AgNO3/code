/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.12.2014 by mbechler
 */
package eu.agno3.orchestrator.gui.connector;


import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Component;

import eu.agno3.orchestrator.gui.msg.addressing.GuisEventScope;
import eu.agno3.runtime.messaging.addressing.EventScope;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.EventMessage;
import eu.agno3.runtime.messaging.msg.impl.TextMessage;


/**
 * @author mbechler
 *
 */
@Component ( service = EventMessage.class )
public class GuiNotificationEvent extends TextMessage<@NonNull MessageSource> implements EventMessage<@NonNull MessageSource> {

    private static final String PATH_PROPERTY = "path"; //$NON-NLS-1$


    /**
     * 
     */
    public GuiNotificationEvent () {
        super();
    }


    /**
     * @param origin
     */
    public GuiNotificationEvent ( @NonNull MessageSource origin ) {
        super(origin);
    }


    /**
     * @param origin
     * @param path
     */
    public GuiNotificationEvent ( @NonNull MessageSource origin, String path ) {
        this(origin, path, StringUtils.EMPTY);
    }


    /**
     * @param origin
     * @param path
     * @param payload
     */
    public GuiNotificationEvent ( @NonNull MessageSource origin, String path, String payload ) {
        super(origin);
        this.getProperties().put(PATH_PROPERTY, path);
        this.setText(payload);
    }


    /**
     * @return notification object path
     */
    public String getPath () {
        return (String) this.getProperties().get(PATH_PROPERTY);
    }


    /**
     * 
     * @return notification payload
     */
    public String getPayload () {
        return this.getText();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.msg.EventMessage#getScopes()
     */
    @Override
    public Collection<EventScope> getScopes () {
        return Arrays.asList((EventScope) new GuisEventScope());
    }

}
