/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.11.2014 by mbechler
 */
package eu.agno3.orchestrator.messaging.server.auth.internal;


import java.security.cert.X509Certificate;

import org.apache.activemq.broker.Broker;
import org.apache.activemq.broker.BrokerFilter;
import org.apache.activemq.broker.ConnectionContext;
import org.apache.activemq.command.ConnectionInfo;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;

import eu.agno3.orchestrator.server.component.auth.AuthConstants;
import eu.agno3.orchestrator.server.component.auth.ComponentPrincipal;


/**
 * @author mbechler
 *
 */
public class AuthenticationBroker extends BrokerFilter implements Broker {

    private static final Logger log = Logger.getLogger(AuthenticationBroker.class);
    private ClientCertificateHandlerImpl certHandler;


    /**
     * @param next
     * @param certHandler
     */
    public AuthenticationBroker ( Broker next, ClientCertificateHandlerImpl certHandler ) {
        super(next);
        this.certHandler = certHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @throws Exception
     * 
     * @see org.apache.activemq.broker.BrokerFilter#addConnection(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConnectionInfo)
     */
    @Override
    public void addConnection ( ConnectionContext context, ConnectionInfo info ) throws Exception {
        if ( context.getSecurityContext() == null ) {

            if ( log.isDebugEnabled() ) {
                log.debug(String.format("Login requested [user=%s ip=%s]", info.getUserName(), info.getClientIp())); //$NON-NLS-1$
            }

            if ( !context.isNetworkConnection() && AuthConstants.SYSTEM_USER.equals(info.getUserName()) ) {
                context.setSecurityContext(new SystemSecurityContextImpl());
                super.addConnection(context, info);
                return;
            }

            if ( info.getUserName() == null ) {
                logAndThrow("Anonymous access disallowed", info); //$NON-NLS-1$
            }

            X509Certificate[] chain = getCertificateChain(context, info);
            X509Certificate primary = chain[ 0 ];
            X500Name dn = new JcaX509CertificateHolder(primary).getSubject();

            ComponentPrincipal princ = PrincipalFactory.getComponentPrincipal(dn);
            ComponentPrincipal toMatch = PrincipalFactory.fromUserName(info.getUserName());

            if ( !princ.equals(toMatch) ) {
                if ( log.isDebugEnabled() ) {
                    log.debug(String.format("From cert %s , from user %s", princ, toMatch)); //$NON-NLS-1$
                }
                logAndThrow("Mismatch between username and certificate data", info); //$NON-NLS-1$
            }

            this.certHandler.haveValid(princ, dn, primary, chain);

            context.setSecurityContext(new ComponentSecurityContextImpl(princ, primary, chain));
        }

        super.addConnection(context, info);
    }


    /**
     * @param string
     */
    private static void logAndThrow ( String msg, ConnectionInfo info ) {
        String logMessage = String.format("%s [user=%s ip=%s]", msg, info.getUserName(), info.getClientIp()); //$NON-NLS-1$
        log.warn(logMessage);
        throw new SecurityException(msg);
    }


    /**
     * @param context
     * @param info
     * @param ctx
     * @return
     */
    protected X509Certificate[] getCertificateChain ( ConnectionContext context, ConnectionInfo info ) {
        Object transportContext = info.getTransportContext();
        if ( ! ( transportContext instanceof X509Certificate[] ) ) {
            logAndThrow("Not a SSL connection", info); //$NON-NLS-1$
        }

        X509Certificate[] chain = (X509Certificate[]) transportContext;

        if ( chain.length == 0 ) {
            logAndThrow("No certificate present", info); //$NON-NLS-1$
        }
        return chain;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.activemq.broker.BrokerFilter#removeConnection(org.apache.activemq.broker.ConnectionContext,
     *      org.apache.activemq.command.ConnectionInfo, java.lang.Throwable)
     */
    @Override
    public void removeConnection ( ConnectionContext context, ConnectionInfo info, Throwable error ) throws Exception {
        super.removeConnection(context, info, error);
        context.setSecurityContext(null);
    }
}
