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
import java.util.List;
import java.util.Map;

import org.apache.felix.service.command.CommandSession;
import org.apache.felix.service.command.Function;
import org.apache.log4j.Logger;
import org.apache.sshd.common.channel.PtyMode;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.command.Command;
import org.jline.terminal.Attributes;
import org.jline.terminal.Attributes.ControlChar;
import org.jline.terminal.Attributes.InputFlag;
import org.jline.terminal.Attributes.LocalFlag;
import org.jline.terminal.Attributes.OutputFlag;
import org.jline.terminal.Size;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import eu.agno3.runtime.console.Console;
import eu.agno3.runtime.console.ConsoleFactory;
import eu.agno3.runtime.console.ShutdownHandler;


class KarafShell implements Command {

    private static final Logger log = Logger.getLogger(KarafShell.class);

    /**
     * 
     */
    private final ConsoleFactory consoleFactory;
    /**
     * 
     */
    private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
    private Console console;
    private PrintStream out;
    private InputStream in;
    ExitCallback exitHandler;


    /**
     * @param consoleFactory
     */
    public KarafShell ( ConsoleFactory consoleFactory ) {
        this.consoleFactory = consoleFactory;
    }


    @Override
    public void start ( final Environment env ) throws IOException {

        log.debug("Initializing console for SSH connection"); //$NON-NLS-1$

        int width = Integer.parseInt(env.getEnv().get(Environment.ENV_COLUMNS));
        int height = Integer.parseInt(env.getEnv().get(Environment.ENV_LINES));

        try {
            @SuppressWarnings ( "resource" )
            // not an actual PTY, so no need to close
            Terminal t = TerminalBuilder.builder().name(String.format("ssh-%s", env.getEnv().get(Environment.ENV_USER))).system(false) //$NON-NLS-1$
                    .streams(this.in, this.out).size(new Size(width, height)).type(env.getEnv().get("TERM")).build(); //$NON-NLS-1$

            mapControls(t, env);
            this.console = this.consoleFactory.createConsole(t);
        }
        catch ( Exception e ) {
            log.error("Failed to create terminal:", e); //$NON-NLS-1$
        }

        this.console.setShutdownHandler(new ShutdownHandler() {

            @Override
            public void shutdown () {
                KarafShell.this.exitHandler.onExit(0);
            }
        });

        setupTerminalSizeEnv(env);

        log.debug("Launching console for SSH connection"); //$NON-NLS-1$
        this.console.start();
    }


    /**
     * 
     * Shamelessly stolen from apache-felix ShellFactoryImpl, Apache License
     * 
     * @param t
     */
    private static void mapControls ( Terminal t, final Environment env ) {
        Attributes attr = t.getAttributes();
        for ( Map.Entry<PtyMode, Integer> e : env.getPtyModes().entrySet() ) {
            switch ( e.getKey() ) {
            case VINTR:
                attr.setControlChar(ControlChar.VINTR, e.getValue());
                break;
            case VQUIT:
                attr.setControlChar(ControlChar.VQUIT, e.getValue());
                break;
            case VERASE:
                attr.setControlChar(ControlChar.VERASE, e.getValue());
                break;
            case VKILL:
                attr.setControlChar(ControlChar.VKILL, e.getValue());
                break;
            case VEOF:
                attr.setControlChar(ControlChar.VEOF, e.getValue());
                break;
            case VEOL:
                attr.setControlChar(ControlChar.VEOL, e.getValue());
                break;
            case VEOL2:
                attr.setControlChar(ControlChar.VEOL2, e.getValue());
                break;
            case VSTART:
                attr.setControlChar(ControlChar.VSTART, e.getValue());
                break;
            case VSTOP:
                attr.setControlChar(ControlChar.VSTOP, e.getValue());
                break;
            case VSUSP:
                attr.setControlChar(ControlChar.VSUSP, e.getValue());
                break;
            case VDSUSP:
                attr.setControlChar(ControlChar.VDSUSP, e.getValue());
                break;
            case VREPRINT:
                attr.setControlChar(ControlChar.VREPRINT, e.getValue());
                break;
            case VWERASE:
                attr.setControlChar(ControlChar.VWERASE, e.getValue());
                break;
            case VLNEXT:
                attr.setControlChar(ControlChar.VLNEXT, e.getValue());
                break;
            /*
             * case VFLUSH:
             * attr.setControlChar(ControlChar.VMIN, e.getValue());
             * break;
             * case VSWTCH:
             * attr.setControlChar(ControlChar.VTIME, e.getValue());
             * break;
             */
            case VSTATUS:
                attr.setControlChar(ControlChar.VSTATUS, e.getValue());
                break;
            case VDISCARD:
                attr.setControlChar(ControlChar.VDISCARD, e.getValue());
                break;
            case ECHO:
                attr.setLocalFlag(LocalFlag.ECHO, e.getValue() != 0);
                break;
            case ICANON:
                attr.setLocalFlag(LocalFlag.ICANON, e.getValue() != 0);
                break;
            case ISIG:
                attr.setLocalFlag(LocalFlag.ISIG, e.getValue() != 0);
                break;
            case ICRNL:
                attr.setInputFlag(InputFlag.ICRNL, e.getValue() != 0);
                break;
            case INLCR:
                attr.setInputFlag(InputFlag.INLCR, e.getValue() != 0);
                break;
            case IGNCR:
                attr.setInputFlag(InputFlag.IGNCR, e.getValue() != 0);
                break;
            case OCRNL:
                attr.setOutputFlag(OutputFlag.OCRNL, e.getValue() != 0);
                break;
            case ONLCR:
                attr.setOutputFlag(OutputFlag.ONLCR, e.getValue() != 0);
                break;
            case ONLRET:
                attr.setOutputFlag(OutputFlag.ONLRET, e.getValue() != 0);
                break;
            case OPOST:
                attr.setOutputFlag(OutputFlag.OPOST, e.getValue() != 0);
                break;

            default:
                // ignore
                break;
            }
        }
        t.setAttributes(attr);
    }


    /**
     * @param env
     */
    private void setupTerminalSizeEnv ( final Environment env ) {
        if ( env.getEnv().containsKey(Environment.ENV_LINES) ) {
            this.console.getSession().put("#LINES", new Function() { //$NON-NLS-1$

                @Override
                public Object execute ( CommandSession session, List<Object> arguments ) {
                    return env.getEnv().get(Environment.ENV_LINES);

                }

            });
        }

        if ( env.getEnv().containsKey(Environment.ENV_COLUMNS) ) {
            this.console.getSession().put("#COLUMNS", new Function() { //$NON-NLS-1$

                @Override
                public Object execute ( CommandSession session, List<Object> arguments ) {
                    return env.getEnv().get(Environment.ENV_COLUMNS);

                }

            });
        }
    }


    @Override
    public void destroy () {
        if ( this.console != null ) {
            this.console.exit();
        }
    }


    @Override
    public void setOutputStream ( OutputStream out ) {
        try {
            this.out = new PrintStream(new LineEndNormalizer(out), true, UTF_8);
        }
        catch ( UnsupportedEncodingException e ) {
            log.warn("Failed to setup output stream:", e); //$NON-NLS-1$
        }
    }


    @Override
    public void setInputStream ( InputStream in ) {
        this.in = in;
    }


    @Override
    public void setErrorStream ( OutputStream err ) {}


    @Override
    public void setExitCallback ( ExitCallback cb ) {
        this.exitHandler = cb;
    }

}