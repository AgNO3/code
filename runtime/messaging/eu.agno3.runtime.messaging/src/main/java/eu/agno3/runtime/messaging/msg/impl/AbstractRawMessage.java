/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.06.2014 by mbechler
 */
package eu.agno3.runtime.messaging.msg.impl;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.msg.Message;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractRawMessage <@NonNull T extends MessageSource> extends AbstractMessage<T> {

    private Map<String, Object> properties = new HashMap<>();


    protected AbstractRawMessage ( @NonNull T origin, int ttl ) {
        super(origin, ttl);
    }


    protected AbstractRawMessage ( @NonNull T origin, Message<@NonNull ? extends MessageSource> replyTo ) {
        this(origin, replyTo != null ? replyTo.getTTL() - 1 : DEFAULT_TTL);
    }


    protected AbstractRawMessage ( @NonNull T origin ) {
        this(origin, DEFAULT_TTL);
    }


    /**
     * 
     */
    public AbstractRawMessage () {
        super();
    }


    /**
     * @return the properties
     */
    public Map<String, Object> getProperties () {
        return this.properties;
    }


    /**
     * @param properties
     *            the properties to set
     */
    public void setProperties ( Map<String, Object> properties ) {
        this.properties = properties;
    }

}
