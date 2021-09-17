/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Aug 6, 2016 by mbechler
 */
package eu.agno3.runtime.security.login;


import java.io.Serializable;
import java.util.Arrays;


/**
 * @author mbechler
 *
 */
public class MessageLoginChallenge extends AbstractLoginChallenge<Object> {

    /**
     * 
     */
    private static final long serialVersionUID = -5585857906390255755L;

    private String messageId;
    private Serializable[] messageArgs;
    private String severity = "info"; //$NON-NLS-1$


    /**
     * 
     * @param id
     *            challenge id
     * @param msgId
     *            message id
     * @param a
     */
    public MessageLoginChallenge ( String id, String msgId, Serializable... a ) {
        super(id, false, null, null);
        this.messageId = msgId;
        if ( a != null ) {
            this.messageArgs = Arrays.copyOf(a, a.length);
        }
        else {
            this.messageArgs = new Serializable[0];
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.AbstractLoginChallenge#isSecret()
     */
    @Override
    protected boolean isSecret () {
        return false;
    }


    /**
     * @param msgId
     * @param a
     * 
     */
    public MessageLoginChallenge ( String msgId, Serializable... a ) {
        this(msgId, msgId, a);
    }


    /**
     * @return message id
     */
    public String getMessageId () {
        return this.messageId;
    }


    /**
     * @return message severity
     */
    public String getSeverity () {
        return this.severity; // $NON-NLS-1$
    }


    /**
     * @return the args
     */
    public Serializable[] getMessageArgs () {
        return this.messageArgs;
    }


    /**
     * 
     * @return first argument
     */
    public Serializable getArgument1 () {
        return this.messageArgs.length > 0 ? this.messageArgs[ 0 ] : null;
    }


    /**
     * 
     * @return second argument
     */
    public Serializable getArgument2 () {
        return this.messageArgs.length > 1 ? this.messageArgs[ 1 ] : null;
    }


    /**
     * 
     * @return third argument
     */
    public Serializable getArgument3 () {
        return this.messageArgs.length > 2 ? this.messageArgs[ 2 ] : null;
    }


    /**
     * 
     * @return fourth argument
     */
    public Serializable getArgument4 () {
        return this.messageArgs.length > 3 ? this.messageArgs[ 3 ] : null;
    }


    /**
     * 
     * @return fifth argument
     */
    public Serializable getArgument5 () {
        return this.messageArgs.length > 4 ? this.messageArgs[ 4 ] : null;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.security.login.LoginChallenge#getType()
     */
    @Override
    public String getType () {
        return "message"; //$NON-NLS-1$
    }

}
