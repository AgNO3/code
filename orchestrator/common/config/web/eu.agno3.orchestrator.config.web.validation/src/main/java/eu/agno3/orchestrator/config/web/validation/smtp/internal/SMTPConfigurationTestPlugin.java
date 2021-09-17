/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.orchestrator.config.web.validation.smtp.internal;


import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;

import javax.mail.AuthenticationFailedException;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.sun.mail.smtp.SMTPAddressFailedException;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPSenderFailedException;
import com.sun.mail.util.MailConnectException;

import eu.agno3.orchestrator.config.model.base.exceptions.ModelServiceException;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestAsyncHandler;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestContext;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginAsync;
import eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPluginRunOn;
import eu.agno3.orchestrator.config.model.validation.ConfigTestParams;
import eu.agno3.orchestrator.config.model.validation.ConfigTestResult;
import eu.agno3.orchestrator.config.model.validation.ConfigTestState;
import eu.agno3.orchestrator.config.web.SMTPConfiguration;
import eu.agno3.orchestrator.config.web.SMTPConfigurationTestParams;
import eu.agno3.orchestrator.config.web.validation.SSLEndpointConfigTestFactory;
import eu.agno3.orchestrator.config.web.validation.SocketValidationUtils;
import eu.agno3.orchestrator.config.web.validation.TLSTestContext;
import eu.agno3.orchestrator.config.web.validation.TLSValidationUtils;
import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPTransportFactory;


/**
 * @author mbechler
 *
 */
@Component ( service = ConfigTestPlugin.class )
public class SMTPConfigurationTestPlugin implements ConfigTestPluginAsync<SMTPConfiguration> {

    private static final Logger log = Logger.getLogger(SMTPConfigurationTestPlugin.class);

    private SMTPTransportFactory transportFactory;
    private SSLEndpointConfigTestFactory sslEndpointFactory;


    @Override
    public Set<ConfigTestPluginRunOn> getRunOn () {
        return EnumSet.of(ConfigTestPluginRunOn.SERVER, ConfigTestPluginRunOn.AGENT);
    }


    @Reference
    protected synchronized void setTransportFactory ( SMTPTransportFactory tf ) {
        this.transportFactory = tf;
    }


    protected synchronized void unsetTransportFactory ( SMTPTransportFactory tf ) {
        if ( this.transportFactory == tf ) {
            this.transportFactory = null;
        }
    }


    @Reference
    protected synchronized void setSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        this.sslEndpointFactory = sectf;
    }


    protected synchronized void unsetSSLEndpointFactory ( SSLEndpointConfigTestFactory sectf ) {
        if ( this.sslEndpointFactory == sectf ) {
            this.sslEndpointFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.config.model.realm.validation.ConfigTestPlugin#getTargetType()
     */
    @Override
    public Class<SMTPConfiguration> getTargetType () {
        return SMTPConfiguration.class;
    }


    @Override
    public ConfigTestResult testAsync ( SMTPConfiguration config, ConfigTestContext ctx, ConfigTestParams params, ConfigTestResult r,
            ConfigTestAsyncHandler h ) throws ModelServiceException {
        log.debug("Running SMTP test"); //$NON-NLS-1$

        if ( ! ( params instanceof SMTPConfigurationTestParams ) ) {
            throw new ModelServiceException("Invalid test parameters"); //$NON-NLS-1$
        }

        SMTPConfigurationTestParams p = (SMTPConfigurationTestParams) params;
        String targetAddress = p.getTargetAddress();

        if ( StringUtils.isBlank(targetAddress) ) {
            throw new ModelServiceException("Missing target address"); //$NON-NLS-1$
        }

        String host = config.getServerUri().getHost();
        int port = config.getServerUri().getPort();
        if ( SocketValidationUtils.checkDNSLookup(r, host) == null ) {
            return r.state(ConfigTestState.FAILURE);
        }

        TLSTestContext tc;
        try {
            tc = this.sslEndpointFactory.adaptSSLClient(config.getSslClientConfiguration());
            TLSValidationUtils.checkTruststoreUsage(tc, r);
        }
        catch ( CryptoException e ) {
            log.debug("Failed to create ssl parameters", e); //$NON-NLS-1$
            return r.state(ConfigTestState.FAILURE).error("SSL_CONFIG", host, e.getMessage()); //$NON-NLS-1$
        }

        String ep = String.format("%s:%d", host, port); //$NON-NLS-1$

        SMTPConfigurationAdapter sca = new SMTPConfigurationAdapter(config);
        try ( SMTPClientTransport ct = this.transportFactory.createTransport(sca, tc.getContext()) ) {
            doConnect(r, h, ep, ct);
            return doSendMessage(r, ctx, targetAddress, ct);
        }
        catch ( MailConnectException e ) {
            return handleConnectException(r, tc, ep, e);
        }
        catch ( AuthenticationFailedException e ) {
            return handleAuthException(r, sca, e);
        }
        catch (
            MessagingException |
            CryptoException e ) {
            return handleOtherException(r, tc, e);
        }
    }


    /**
     * @param r
     * @param host
     * @param e
     * @return
     */
    private static ConfigTestResult handleConnectException ( ConfigTestResult r, TLSTestContext tc, String ep, MailConnectException e ) {
        if ( e.getCause() instanceof SSLException ) {
            TLSValidationUtils.handleTLSException((SSLException) e.getCause(), r, tc);
        }
        else if ( e.getCause() instanceof IOException ) {
            SocketValidationUtils.handleIOException((IOException) e.getCause(), r, ep);
        }
        else {
            log.debug("Unknown connect exception", e); //$NON-NLS-1$
            r.error("FAIL_SMTP_CONNECT_UNKNOWN", e.getMessage()); //$NON-NLS-1$
        }
        return r.state(ConfigTestState.FAILURE);
    }


    /**
     * @param r
     * @param sca
     * @param e
     * @return
     */
    private static ConfigTestResult handleAuthException ( ConfigTestResult r, SMTPConfigurationAdapter sca, AuthenticationFailedException e ) {
        log.debug("Authentication failed: " + e.getMessage(), e); //$NON-NLS-1$
        if ( !sca.isAuthEnabled() ) {
            r.error("FAIL_SMTP_NOAUTH", e.getMessage()); //$NON-NLS-1$
        }
        else {
            r.error("FAIL_SMTP_AUTH", sca.getSMTPUser(), sca.getAuthMechanisms().toString(), e.getMessage()); //$NON-NLS-1$
        }
        return r.state(ConfigTestState.FAILURE);
    }


    /**
     * @param r
     * @param tc
     * @param e
     * @return
     */
    private static ConfigTestResult handleOtherException ( ConfigTestResult r, TLSTestContext tc, Exception e ) {
        if ( e instanceof MessagingException && e.getCause() instanceof IOException && e.getCause().getCause() instanceof SSLException ) {
            log.trace("Original exception", e); //$NON-NLS-1$
            TLSValidationUtils.handleTLSException((SSLException) e.getCause().getCause(), r, tc);
        }
        else if ( e instanceof MessagingException && e.getCause() instanceof SSLHandshakeException ) {
            log.trace("Original exception", e); //$NON-NLS-1$
            TLSValidationUtils.handleHandshakeException((SSLHandshakeException) e.getCause(), r, tc);
        }
        else if ( e instanceof MessagingException && e.getCause() instanceof SSLPeerUnverifiedException ) {
            TLSValidationUtils.handleTLSNameMismatch(e, r, tc);
        }
        else {
            log.debug("Unspecified failure", e); //$NON-NLS-1$
            r.error("FAIL_UNKNOWN", e.getMessage()); //$NON-NLS-1$
        }
        return r.state(ConfigTestState.FAILURE);
    }


    /**
     * @param r
     * @param h
     * @param host
     * @param port
     * @param ct
     * @throws MessagingException
     */
    private static void doConnect ( ConfigTestResult r, ConfigTestAsyncHandler h, String ep, SMTPClientTransport ct ) throws MessagingException {
        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Connecting transport %s", ep)); //$NON-NLS-1$
        }
        h.update(r.info("CONNECTING", ep)); //$NON-NLS-1$
        ct.connect();
        log.debug("Connected transport"); //$NON-NLS-1$

        if ( ct.isStartTLSEstablished() ) {
            log.debug("StartTLS channel is established"); //$NON-NLS-1$
            h.update(r.info("CONNECTED_STARTTLS", ep)); //$NON-NLS-1$
        }
        else {
            h.update(r.info("CONNECTED", ep)); //$NON-NLS-1$
        }
    }


    /**
     * @param r
     * @param ctx
     * @param targetAddress
     * @param ct
     * @return
     */
    private static ConfigTestResult doSendMessage ( ConfigTestResult r, ConfigTestContext ctx, String targetAddress, SMTPClientTransport ct ) {
        try {
            ct.sendMessage(createTestMessage(targetAddress, ctx, ct));
            if ( log.isDebugEnabled() ) {
                log.debug("Sent message to " + targetAddress); //$NON-NLS-1$
            }
            r.info("SMTP_SENT_MESSAGE", targetAddress); //$NON-NLS-1$
            return r.state(ConfigTestState.SUCCESS);
        }
        catch ( SendFailedException e ) {
            handleSendFailure(e, r);
            return r.state(ConfigTestState.FAILURE);
        }
        catch ( MessagingException e ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Failed to send test message to " + targetAddress, e); //$NON-NLS-1$
            }
            r.error("FAIL_SMTP_SEND_UNKNOWN", e.getMessage()); //$NON-NLS-1$
            return r.state(ConfigTestState.FAILURE);
        }
    }


    /**
     * @param e
     * @param r
     */
    private static void handleSendFailure ( SendFailedException e, ConfigTestResult r ) {
        if ( e.getCause() instanceof SMTPAddressFailedException ) {
            SMTPAddressFailedException ae = (SMTPAddressFailedException) e.getCause();
            log.debug(String.format("Destination address %s rejected: %s", ae.getAddress(), ae.getMessage()), e.getCause()); //$NON-NLS-1$
            r.error("FAIL_SMTP_DESTINATION_REJECTED", ae.getAddress().toString(), ae.getMessage()); //$NON-NLS-1$
        }
        else if ( e.getCause() instanceof SMTPSenderFailedException ) {
            SMTPSenderFailedException se = (SMTPSenderFailedException) e.getCause();
            log.debug(String.format("Sender address %s rejected: %s", se.getAddress(), se.getMessage()), e.getCause()); //$NON-NLS-1$
            r.error("FAIL_SMTP_SENDER_REJECTED", se.getAddress().toString(), se.getMessage()); //$NON-NLS-1$
        }
        else if ( e.getCause() instanceof SMTPSendFailedException ) {
            SMTPSendFailedException se = (SMTPSendFailedException) e.getCause();
            log.debug("Send rejected: " + se.getMessage(), se); //$NON-NLS-1$
            r.error("FAIL_SMTP_SEND", se.getMessage()); //$NON-NLS-1$
        }
        else {
            log.debug("Unspecified failure while sending mail", e); //$NON-NLS-1$
            r.error("FAIL_SMTP_SEND_UNKNOWN", e.getMessage()); //$NON-NLS-1$
        }
    }


    /**
     * @param targetAddress
     * @param ctx
     * @param ct
     * @return
     * @throws MessagingException
     * @throws AddressException
     */
    private static MimeMessage createTestMessage ( String targetAddress, ConfigTestContext ctx, SMTPClientTransport ct )
            throws MessagingException, AddressException {
        MimeMessage msg = ct.createMimeMessage();
        msg.setRecipient(RecipientType.TO, new InternetAddress(targetAddress));
        msg.setSubject("Test Message"); //$NON-NLS-1$

        String dateFormatted = DateTimeFormat.mediumDateTime().print(DateTime.now());

        String userFormatted;
        if ( ctx.getOwner() != null ) {
            userFormatted = String.format("on behalf of " + ctx.getOwner()); //$NON-NLS-1$
        }
        else {
            userFormatted = String.format("on behalf of an unknown user"); //$NON-NLS-1$
        }

        msg.setText(String.format("This test message was sent %s %s", dateFormatted, userFormatted)); //$NON-NLS-1$
        return msg;
    }

}
