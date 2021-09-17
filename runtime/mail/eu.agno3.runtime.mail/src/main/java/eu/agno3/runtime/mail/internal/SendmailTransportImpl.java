/**
 * © 2017 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Oct 13, 2017 by mbechler
 */
package eu.agno3.runtime.mail.internal;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import eu.agno3.runtime.mail.SMTPClientTransport;
import eu.agno3.runtime.mail.SMTPConfiguration;


/**
 * @author mbechler
 *
 */
public class SendmailTransportImpl extends SMTPTransportImpl implements SMTPClientTransport {

    /**
     * 
     */
    private static final String USR_BIN_SENDMAIL = "/usr/bin/sendmail"; //$NON-NLS-1$
    private static final Logger log = Logger.getLogger(SendmailTransportImpl.class);
    private final String sendmailPath;
    private final List<String> extraArguments;
    private boolean addSender;


    /**
     * @param sess
     * @param cfg
     * @throws MessagingException
     * 
     */
    public SendmailTransportImpl ( Session sess, SMTPConfiguration cfg ) throws MessagingException {
        this(
            sess,
            cfg,
            cfg.getSendmailPath() != null ? cfg.getSendmailPath() : USR_BIN_SENDMAIL,
            cfg.isSetSendmailSender(),
            cfg.getSendmailExtraArgs() != null ? cfg.getSendmailExtraArgs().toArray(new String[0]) : new String[0]);
    }


    /**
     * @param sess
     * @param cfg
     * @param sendmailPath
     * @param addSender
     * @param extraArgs
     * @throws MessagingException
     * 
     */
    public SendmailTransportImpl ( Session sess, SMTPConfiguration cfg, String sendmailPath, boolean addSender, String... extraArgs )
            throws MessagingException {
        super(sess, cfg);
        this.sendmailPath = sendmailPath;
        this.addSender = addSender;
        this.extraArguments = Arrays.asList(extraArgs);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPClientTransport#sendMessage(javax.mail.internet.MimeMessage, java.util.List)
     */
    @Override
    public void sendMessage ( MimeMessage msg, List<InternetAddress> addresses ) throws MessagingException {
        ClassLoader oldTTCL = Thread.currentThread().getContextClassLoader();
        try {
            log.debug("Preparing message for sending"); //$NON-NLS-1$
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            SMTPTransportImpl.prepareMessage(getConfig(), msg, addresses);
            sendMessage(msg, addresses.toArray(new Address[] {}));
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldTTCL);
        }
    }


    @Override
    public synchronized void sendMessage ( Message message, Address[] addresses ) throws MessagingException, SendFailedException {

        if ( !Files.isExecutable(Paths.get(this.sendmailPath)) ) {
            throw new MessagingException("Sendmail not found at " + this.sendmailPath); //$NON-NLS-1$
        }
        if ( ! ( message instanceof MimeMessage ) ) {
            throw new MessagingException("Sendmail can only send RFC822 messages"); //$NON-NLS-1$
        }
        if ( addresses.length == 0 ) {
            throw new SendFailedException("No recipient addresses"); //$NON-NLS-1$
        }

        List<String> args = new ArrayList<>();
        args.add(this.sendmailPath);
        args.add("-bm"); //$NON-NLS-1$

        if ( this.addSender ) {
            args.add("-F"); //$NON-NLS-1$
            args.add(getConfig().getDefaultFromName());

            args.add("-f"); //$NON-NLS-1$
            args.add(getConfig().getDefaultFromAddress());
        }

        args.add("-ep"); //$NON-NLS-1$

        args.addAll(this.extraArguments);

        for ( Address addr : addresses ) {
            if ( ! ( addr instanceof InternetAddress ) ) {
                throw new MessagingException(addr + " is not an InternetAddress"); //$NON-NLS-1$
            }
            InternetAddress ia = (InternetAddress) addr;
            args.add(ia.getAddress());
        }

        // 8bit mime? -B8BITMIME

        File devnull = new File("/dev/null"); //$NON-NLS-1$

        ProcessBuilder pb = new ProcessBuilder(args);
        pb.environment().clear();
        pb.redirectOutput(Redirect.to(devnull));
        pb.redirectError(Redirect.PIPE);
        pb.redirectInput(Redirect.PIPE);

        try {
            Process proc = pb.start();

            BufferedReader err;
            try ( OutputStream os = proc.getOutputStream() ) {
                err = new BufferedReader(new InputStreamReader(proc.getErrorStream(), StandardCharsets.US_ASCII));
                message.writeTo(os);
                os.close();
            }

            String line;
            while ( ( line = err.readLine() ) != null ) {
                log.warn("Sendmail: " + line); //$NON-NLS-1$
            }

            int rc = proc.waitFor();
            if ( rc != 0 ) {
                throw new SendFailedException("Failed to send mail, exit code " + rc); //$NON-NLS-1$
            }
        }
        catch (
            IOException |
            InterruptedException e ) {
            throw new SendFailedException("Failed to send mail: " + e.getMessage(), e); //$NON-NLS-1$
        }

    }


    @Override
    public synchronized void connect ( Socket socket ) throws MessagingException {
        log.debug("Ignoring connect"); //$NON-NLS-1$
    }


    @Override
    protected synchronized boolean protocolConnect ( String host, int port, String user, String password ) throws MessagingException {
        log.debug("Ignoring protocolConnect"); //$NON-NLS-1$
        return true;
    }


    @Override
    public synchronized void close () throws MessagingException {
        log.debug("Ignoring close"); //$NON-NLS-1$
    }


    @Override
    public synchronized boolean isConnected () {
        return true;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPClientTransport#sendMessage(javax.mail.internet.MimeMessage)
     */
    @Override
    public void sendMessage ( MimeMessage msg ) throws MessagingException {
        sendMessage(msg, SMTPTransportImpl.getRecipients(msg));
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage()
     */
    @Override
    public MimeMessage createMimeMessage () throws MessagingException {
        return new MimeMessage(this.session);
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPTransportProvider#createMimeMessage(java.io.InputStream)
     */
    @Override
    public MimeMessage createMimeMessage ( InputStream is ) throws MessagingException {
        return new MimeMessage(this.session, is);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.mail.SMTPClientTransport#isStartTLSEstablished()
     */
    @Override
    public boolean isStartTLSEstablished () {
        return false;
    }


    /**
     * {@inheritDoc}
     *
     * @see com.sun.mail.smtp.SMTPTransport#isSSL()
     */
    @Override
    public synchronized boolean isSSL () {
        return false;
    }

}
