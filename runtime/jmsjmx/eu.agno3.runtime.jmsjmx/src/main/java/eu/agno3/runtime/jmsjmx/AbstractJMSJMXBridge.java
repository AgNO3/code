/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.04.2015 by mbechler
 */
package eu.agno3.runtime.jmsjmx;


import java.io.IOException;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.apache.log4j.Logger;
import org.eclipse.jdt.annotation.NonNull;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.messaging.MessagingException;
import eu.agno3.runtime.messaging.addressing.MessageSource;
import eu.agno3.runtime.messaging.listener.MessageProcessingException;
import eu.agno3.runtime.messaging.listener.RequestEndpoint;


/**
 * @author mbechler
 * @param <T>
 * @param <TError>
 *
 */
public abstract class AbstractJMSJMXBridge <T extends AbstractJMXRequest<@NonNull MessageSource, TError>, TError extends JMXErrorResponse>
        implements RequestEndpoint<T, JMXResponse, TError> {

    private static final Logger log = Logger.getLogger(AbstractJMSJMXBridge.class);
    private MessageSource messageSource;
    private MBeanServerConnection mbeanServerConnection;


    /**
     * 
     */
    public AbstractJMSJMXBridge () {
        super();
    }


    @Reference
    protected synchronized void setMbeanServer ( MBeanServer mbs ) {
        this.mbeanServerConnection = mbs;
    }


    protected synchronized void unsetMbeanServer ( MBeanServer mbs ) {
        if ( this.mbeanServerConnection == mbs ) {
            this.mbeanServerConnection = null;
        }
    }


    @Reference
    protected synchronized void setMessageSource ( MessageSource ms ) {
        this.messageSource = ms;
    }


    protected synchronized void unsetMessageSource ( MessageSource ms ) {
        if ( this.messageSource == ms ) {
            this.messageSource = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.messaging.listener.RequestEndpoint#onReceive(eu.agno3.runtime.messaging.msg.RequestMessage)
     */
    @Override
    public JMXResponse onReceive ( @NonNull T msg ) throws MessageProcessingException, MessagingException {

        ClassLoader oldTCCL = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(getClassLoader());
            if ( log.isDebugEnabled() ) {
                log.debug("Recieved message " + msg); //$NON-NLS-1$
            }
            MBeanServerConnection mbs = getMBeanServerConnection(msg);
            try {
                ObjectName targetObjectName = msg.getObjectName();
                if ( mbs == null || targetObjectName == null ) {
                    throw new MessagingException("Not properly set up, either connection or object name is null"); //$NON-NLS-1$
                }
                JMXResponse resp = new JMXResponse(getMessageSource(), msg);

                if ( msg.getType() == AbstractJMXRequest.INVOKE ) {
                    resp.setResponseObject(mbs.invoke(targetObjectName, msg.getName(), msg.getParams(), msg.getSignature()));
                }
                else if ( msg.getType() == AbstractJMXRequest.GET_ATTR ) {
                    resp.setResponseObject(mbs.getAttribute(targetObjectName, msg.getName()));
                }
                else if ( msg.getType() == AbstractJMXRequest.SET_ATTR ) {
                    if ( msg.getSignature() == null || msg.getSignature().length != 1 ) {
                        throw new IOException("Invalid set request"); //$NON-NLS-1$
                    }
                    mbs.setAttribute(targetObjectName, new Attribute(msg.getName(), msg.getParams()[ 0 ]));
                }
                return resp;
            }
            finally {
                if ( mbs instanceof AutoCloseable ) {
                    ( (AutoCloseable) mbs ).close();
                }
            }
        }
        catch ( Exception e ) {
            log.warn("Exception in JMX call", e); //$NON-NLS-1$
            JMXErrorResponse resp = createErrorResponse(msg);
            resp.setThrowable(e);
            throw new MessageProcessingException(resp);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTCCL);
        }
    }


    /**
     * @return
     */
    protected ClassLoader getClassLoader () {
        return this.getClass().getClassLoader();
    }


    protected abstract JMXErrorResponse createErrorResponse ( T msg ) throws MessagingException;


    /**
     * @return
     * @throws MessagingException
     */
    @NonNull
    protected MessageSource getMessageSource () throws MessagingException {
        MessageSource ms = this.messageSource;
        if ( ms == null ) {
            throw new MessagingException("No message source configured"); //$NON-NLS-1$
        }
        return ms;
    }


    /**
     * @param msg
     * @return
     * @throws MessagingException
     * @throws Exception
     */
    protected MBeanServerConnection getMBeanServerConnection ( @NonNull T msg ) throws MessagingException {
        return this.mbeanServerConnection;
    }

}