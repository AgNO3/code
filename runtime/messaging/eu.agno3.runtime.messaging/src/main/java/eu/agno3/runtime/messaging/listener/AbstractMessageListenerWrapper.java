/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.08.2013 by mbechler
 */
package eu.agno3.runtime.messaging.listener;


import org.eclipse.jdt.annotation.NonNull;


/**
 * @author mbechler
 * @param <T>
 * 
 */
public abstract class AbstractMessageListenerWrapper <T> implements MessageListener {

    @NonNull
    private final Class<@NonNull ? extends T> messageType;


    /**
     * 
     * 
     * @param msgType
     */
    public AbstractMessageListenerWrapper ( @NonNull Class<@NonNull ? extends T> msgType ) {
        super();
        this.messageType = msgType;
    }


    /**
     * @return
     */
    protected @NonNull Class<@NonNull ? extends T> getMessageType () {
        return this.messageType;
    }

}