/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.apache.karaf.shell.impl.console.HeadlessSessionImpl;
import org.apache.log4j.Logger;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.SessionAware;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.session.ServerSession;


/**
 * @author mbechler
 * 
 */
public class ConsoleCommandWrapper implements Command, SessionAware {

    private static final Logger log = Logger.getLogger(ConsoleCommandWrapper.class);

    /**
     * 
     */
    private static final String CONSOLE_ENCODING = "UTF-8"; //$NON-NLS-1$

    /**
     * 
     */
    private final CommandProcessor commandProcessor;
    /**
     * 
     */
    private final String cmd;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private Session session;

    private ExitCallback exitCallback;

    private ServerSession sshSession;


    /**
     * @param commandProcessor
     * @param cmd
     */
    public ConsoleCommandWrapper ( CommandProcessor commandProcessor, String cmd ) {
        this.commandProcessor = commandProcessor;
        this.cmd = cmd;
    }


    @Override
    public void start ( Environment env ) throws IOException {
        this.session = new HeadlessSessionImpl((SessionFactory) this.commandProcessor, this.commandProcessor, this.in, this.out, this.err);
        try {
            this.session.execute(this.cmd);
            this.exitCallback.onExit(0);
        }
        catch ( Exception e ) {
            log.warn("Failed to execute command:", e); //$NON-NLS-1$
            e.printStackTrace(this.err);
            this.exitCallback.onExit(-1, e.getMessage());
        }
        finally {
            this.in.close();
            this.out.close();
            this.err.close();
            this.session.close();
            this.sshSession.close();
        }

    }


    @Override
    public void setOutputStream ( OutputStream out ) {
        try {
            this.out = new PrintStream(out, true, CONSOLE_ENCODING);
        }
        catch ( UnsupportedEncodingException e ) {
            log.error("Failed to set output stream: ", e); //$NON-NLS-1$
        }
    }


    @Override
    public void setInputStream ( InputStream in ) {
        this.in = in;
    }


    @Override
    public void setErrorStream ( OutputStream err ) {
        try {
            this.err = new PrintStream(err, true, CONSOLE_ENCODING);
        }
        catch ( UnsupportedEncodingException e ) {
            log.error("Failed to set error stream: ", e); //$NON-NLS-1$
        }
    }


    @Override
    public void setExitCallback ( ExitCallback cb ) {
        this.exitCallback = cb;
    }


    @Override
    public void destroy () {
        if ( this.session != null ) {
            this.session.close();
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.sshd.server.SessionAware#setSession(org.apache.sshd.server.session.ServerSession)
     */
    @Override
    public void setSession ( ServerSession session ) {
        this.sshSession = session;
    }
}