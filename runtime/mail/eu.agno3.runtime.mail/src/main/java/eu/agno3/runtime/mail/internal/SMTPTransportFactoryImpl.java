/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Nov 25, 2016 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;

import eu.agno3.runtime.crypto.CryptoException;
import eu.agno3.runtime.crypto.tls.TLSContext;
import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPConfiguration;
import eu.agno3.runtime.mail.SMTPTransportFactory;
import eu.agno3.runtime.util.log.LogOutputStream;


/**
 * @author mbechler
 *
 */
@Component ( service = SMTPTransportFactory.class )
public class SMTPTransportFactoryImpl implements SMTPTransportFactory {

    private static final Logger log = Logger.getLogger(SMTPTransportFactoryImpl.class);


    /**
     * {@inheritDoc}
     * 
     * @throws CryptoException
     * @throws MessagingException
     *
     * @see eu.agno3.runtime.mail.SMTPTransportFactory#createTransport(eu.agno3.runtime.mail.SMTPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    public SMTPClientTransport createTransport ( SMTPConfiguration cfg, TLSContext tc ) throws CryptoException, MessagingException {
        if ( cfg.isUseSendmail() ) {
            return new SendmailTransportImpl(makeSession(cfg, tc), cfg);
        }
        return new SMTPTransportImpl(makeSession(cfg, tc), cfg, tc);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportFactory#makeSession(eu.agno3.runtime.mail.SMTPConfiguration,
     *      eu.agno3.runtime.crypto.tls.TLSContext)
     */
    @Override
    @SuppressWarnings ( "resource" )
    public Session makeSession ( SMTPConfiguration config, TLSContext tc ) throws CryptoException {
        Session s = Session.getInstance(makeProperties(config, tc), new SMTPAuthenticator(config));
        if ( log.isDebugEnabled() ) {
            s.setDebugOut(new PrintStream(new LogOutputStream(log, Level.DEBUG, Charset.defaultCharset())));
            s.setDebug(true);
        }
        return s;
    }


    /**
     * @return
     * @throws CryptoException
     */
    private static Properties makeProperties ( SMTPConfiguration config, TLSContext tc ) throws CryptoException {
        Properties props = new Properties();

        String prefix = "mail." + config.getInstanceId(); //$NON-NLS-1$

        props.put(prefix + ".host", config.getSMTPHost()); //$NON-NLS-1$
        props.put(prefix + ".port", config.getSMTPPort()); //$NON-NLS-1$

        props.put(prefix + ".connectiontimeout", config.getConnTimeout()); //$NON-NLS-1$
        props.put(prefix + ".timeout", config.getReadTimeout()); //$NON-NLS-1$
        props.put(prefix + ".writetimeout", config.getWriteTimeout()); //$NON-NLS-1$

        if ( !StringUtils.isBlank(config.getEhloHostName()) ) {
            props.put(prefix + ".localhost", config.getEhloHostName()); //$NON-NLS-1$
        }

        if ( config.isStartTLS() ) {
            props.put(prefix + ".starttls.enable", true); //$NON-NLS-1$
        }

        if ( config.isStartTLSRequired() ) {
            props.put(prefix + ".starttls.required", true); //$NON-NLS-1$
        }

        if ( config.isAuthEnabled() ) {
            props.put(prefix + ".auth", true); //$NON-NLS-1$

            if ( config.getAuthMechanisms() != null ) {
                props.put(prefix + ".auth.mechanisms", StringUtils.join(config.getAuthMechanisms(), ' ')); //$NON-NLS-1$
            }
        }
        else {
            props.put(prefix + ".auth", false); //$NON-NLS-1$
            props.put(prefix + ".auth.mechanisms", StringUtils.EMPTY); //$NON-NLS-1$
        }

        if ( !StringUtils.isBlank(config.getDefaultFromAddress()) ) {
            props.put(prefix + ".from", config.getDefaultFromAddress()); //$NON-NLS-1$
        }

        if ( config.getExtraProperties() != null ) {
            for ( Entry<String, String> e : config.getExtraProperties().entrySet() ) {
                props.put(prefix + "." + e.getKey(), e.getValue()); //$NON-NLS-1$
            }
        }

        for ( Entry<Object, Object> o : props.entrySet() ) {
            log.debug(String.format("%s: %s", o.getKey(), o.getValue())); //$NON-NLS-1$
        }

        return props;
    }
}
