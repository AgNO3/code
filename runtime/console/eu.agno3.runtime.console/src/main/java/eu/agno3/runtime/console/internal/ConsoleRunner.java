/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.felix.gogo.runtime.CommandNotFoundException;
import org.apache.karaf.shell.api.console.Function;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.apache.karaf.shell.impl.console.CommandsCompleter;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;

import eu.agno3.runtime.console.Console;
import eu.agno3.runtime.console.ConsoleConfiguration;
import eu.agno3.runtime.console.ShutdownHandler;


/**
 * @author mbechler
 * 
 */
public class ConsoleRunner extends Thread implements Console {

    private static final Logger log = Logger.getLogger(ConsoleRunner.class);

    private ShutdownHandler shutdownHandler;
    private ConsoleConfiguration consoleConfiguration;

    boolean exitFlag = false;

    private Session session;

    Terminal terminal;

    private CommandsCompleter completer;


    /**
     * @param consoleConfiguration
     * @param session
     * @param sessionFactory
     * @param title
     * @param term
     */
    public ConsoleRunner ( ConsoleConfiguration consoleConfiguration, Session session, SessionFactory sessionFactory, String title, Terminal term ) {
        super("Console: " + title); //$NON-NLS-1$
        this.session = session;
        this.terminal = term;
        this.completer = new CommandsCompleter(sessionFactory, session);
        this.consoleConfiguration = consoleConfiguration;

    }


    /**
     * @param consoleConfiguration
     *            the consoleConfiguration to set
     */
    public void setConsoleConfiguration ( ConsoleConfiguration consoleConfiguration ) {
        this.consoleConfiguration = consoleConfiguration;
    }


    /**
     * @param shutdownHandler
     *            the shutdownHandler to set
     */
    @Override
    public void setShutdownHandler ( ShutdownHandler shutdownHandler ) {
        this.shutdownHandler = shutdownHandler;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run () {
        if ( log.isTraceEnabled() ) {
            log.trace("Terminal type: " + this.terminal.getClass().getName()); //$NON-NLS-1$
            log.trace(String.format("Terminal size %dx%d", this.terminal.getWidth(), this.terminal.getHeight())); //$NON-NLS-1$
        }

        try {
            runInternal(setupConsole());
        }
        catch ( Exception e ) {
            log.error("Failed to open console:", e); //$NON-NLS-1$
            return;
        }

    }


    /**
     * @param reader
     */
    private void runInternal ( LineReader reader ) {
        try {
            setupConsole(reader);
            while ( !this.exitFlag ) {
                tryProcessLine(reader);
            }
        }
        catch ( EndOfFileException e ) {
            if ( this.shutdownHandler != null ) {
                this.shutdownHandler.shutdown();

            }
            return;
        }
        catch ( InterruptedException e ) {
            if ( this.shutdownHandler != null ) {
                this.shutdownHandler.shutdown();

            }
            return;
        }
        catch ( InterruptedIOException e ) {
            log.debug("Console read interrupted", e); //$NON-NLS-1$
        }
        catch ( IOException e ) {
            log.warn("Console read error:", e); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Console error:", e); //$NON-NLS-1$
        }
    }


    /**
     * @param reader
     */
    private void setupConsole ( LineReader reader ) {
        this.session.put(Session.SCOPE, this.consoleConfiguration.getScopes());
        this.session.put(".jline.reader", reader); //$NON-NLS-1$
        this.session.put(".jline.terminal", reader.getTerminal()); //$NON-NLS-1$
        this.session.put(".jline.history", reader.getHistory()); //$NON-NLS-1$
        this.session.put(Session.SUBSHELL, StringUtils.EMPTY);
        this.session.put(Session.COMPLETION_MODE, Session.COMPLETION_MODE_GLOBAL);
        this.session.put("#LINES", new Function() { //$NON-NLS-1$

            @Override
            public Object execute ( Session s, List<Object> args ) throws Exception {
                return Integer.toString(ConsoleRunner.this.terminal.getHeight());
            }
        });
        this.session.put("#COLUMNS", new Function() { //$NON-NLS-1$

            @Override
            public Object execute ( Session sess, List<Object> args ) throws Exception {
                return Integer.toString(ConsoleRunner.this.terminal.getWidth());
            }
        });
    }


    /**
     * @return
     * @throws ConsoleException
     * @throws Exception
     */
    protected LineReader setupConsole () throws ConsoleException {
        LineReaderBuilder b = LineReaderBuilder.builder().appName(this.consoleConfiguration.getApplicationName()).terminal(this.terminal);

        if ( this.completer != null ) {
            b.completer(this.completer);
        }

        return b.build();
    }


    /**
     * @param session2
     * @param reader
     * @throws Exception
     */
    protected void tryProcessLine ( LineReader reader ) throws InterruptedException, IOException {
        try {
            String commandLine = reader.readLine(this.consoleConfiguration.getPrompt());
            if ( log.isTraceEnabled() ) {
                log.trace("Console input: " + commandLine); //$NON-NLS-1$
            }

            if ( commandLine != null ) {
                this.session.execute(commandLine);
            }
        }
        catch ( IOException e ) {
            throw e;
        }
        catch ( EndOfFileException e ) {
            throw e;
        }
        catch ( UserInterruptException e ) {
            log.trace("User interrupt:", e); //$NON-NLS-1$
            throw new InterruptedException();
        }
        catch ( CommandNotFoundException e ) {
            log.trace("Command not found", e); //$NON-NLS-1$
            this.session.getConsole().println("Command not found: " + e.getCommand()); //$NON-NLS-1$
        }
        catch ( Exception e ) {
            log.warn("Exception in console execution:", e); //$NON-NLS-1$

            this.session.getConsole().println(Ansi.ansi().bold().fg(Ansi.Color.RED).a(e.getClass().getName()).a(": ").a(e.getMessage()) //$NON-NLS-1$
                    .fg(Ansi.Color.DEFAULT).boldOff());
        }
    }


    /**
     * 
     */
    @Override
    public void exit () {
        this.exitFlag = true;
        this.interrupt();
        try {
            this.join();
        }
        catch ( InterruptedException e ) {
            log.debug("Shutdown interrupted:", e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.console.Console#getSession()
     */
    @Override
    public Session getSession () {
        return this.session;
    }
}
