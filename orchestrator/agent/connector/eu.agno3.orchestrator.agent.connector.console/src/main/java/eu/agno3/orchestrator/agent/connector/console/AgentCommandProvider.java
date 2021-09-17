/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.connector.console;


import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.apache.log4j.Logger;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.connector.AgentServerConnector;
import eu.agno3.orchestrator.server.connector.ServerConnectorException;
import eu.agno3.orchestrator.server.connector.ServerConnectorState;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( immediate = true )
public class AgentCommandProvider implements CommandProvider {

    private static final Logger log = Logger.getLogger(AgentCommandProvider.class);

    /**
     * Timeout for waiting for connection
     */
    public static final long TIMEOUT = 1000 * 60 * 30;
    private AgentServerConnector connector;

    private static final String STATE = "State: "; //$NON-NLS-1$
    private static final String SERVER_CONN_UNAVAIL = "Server connector not available"; //$NON-NLS-1$


    @Reference ( policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setServerConnector ( AgentServerConnector c ) {
        this.connector = c;
    }


    protected synchronized void unsetServerConnector ( AgentServerConnector c ) {
        if ( this.connector == c ) {
            this.connector = null;
        }
    }


    /**
     * @return the connector
     */
    synchronized AgentServerConnector getConnector () {
        return this.connector;
    }


    /**
     * @return
     */
    Ansi.Color getStatusColor ( ServerConnectorState state ) {
        Ansi.Color stateColor = Ansi.Color.RED;

        if ( state == ServerConnectorState.CONNECTED ) {
            stateColor = Color.GREEN;
        }
        else if ( state == ServerConnectorState.CONNECTING || state == ServerConnectorState.DISCONNECTING ) {
            stateColor = Color.YELLOW;
        }
        else if ( state == ServerConnectorState.DISCONNECTED ) {
            stateColor = Color.MAGENTA;
        }
        return stateColor;
    }


    /**
     * @return the log
     */
    static Logger getLog () {
        return log;
    }

    /**
     * Show server connector status
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "connector", name = "status", description = "Show server connector status" )
    public class StatusCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            Ansi out = Ansi.ansi();

            if ( getConnector() == null ) {
                out.bold().a(SERVER_CONN_UNAVAIL).boldOff().newline();
            }
            else {
                out.bold().a("Agent ID: ").boldOff() //$NON-NLS-1$

                        .fg(Ansi.Color.BLUE).a(getConnector().getComponentId().toString()).fg(Ansi.Color.DEFAULT).newline();

                out.bold().a("Server: ").boldOff() //$NON-NLS-1$
                        .fg(Ansi.Color.BLUE).a(getConnector().getServerAddress()).fg(Ansi.Color.DEFAULT).newline();

                Ansi.Color stateColor = getStatusColor(getConnector().getState());

                out.bold().a(STATE).fg(stateColor).a(getConnector().getState()).fg(Color.DEFAULT).boldOff().newline();

            }

            this.session.getConsole().print(out);

            return null;
        }
    }

    /**
     * Connect to server
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "connector", name = "connect", description = "Connect to server" )
    public class ConnectCommand implements Action {

        /**
         * 
         */
        private static final String SERVER_CONN_INIT_FAIL = "Failed to initialize server connection:"; //$NON-NLS-1$

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws InterruptedException {

            if ( getConnector() == null ) {
                this.session.getConsole().println(SERVER_CONN_UNAVAIL);
            }
            else {

                long start = System.currentTimeMillis();
                long end = start + TIMEOUT;

                try {
                    getConnector().connect();
                }
                catch ( ServerConnectorException e ) {
                    getLog().warn(SERVER_CONN_INIT_FAIL, e);
                    this.session.getConsole().println(SERVER_CONN_INIT_FAIL);
                }

                while ( System.currentTimeMillis() < end
                        && ! ( getConnector().getState() == ServerConnectorState.ERROR || getConnector().getState() == ServerConnectorState.CONNECTED ) ) {
                    Thread.sleep(100);
                }

                this.session.getConsole().print(
                    Ansi.ansi().bold().a(STATE).fg(getStatusColor(getConnector().getState())).a(getConnector().getState()).fg(Color.DEFAULT)
                            .boldOff().newline());

            }

            return null;
        }

    }

    /**
     * Disconnect from server
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "connector", name = "disconnect", description = "Disconnect from server" )
    public class DisconnectCommand implements Action {

        /**
         * 
         */
        private static final String SERVER_DISCONN_FAIL = "Failed to initialize server disconnection:"; //$NON-NLS-1$

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () throws InterruptedException {

            if ( getConnector() == null ) {
                this.session.getConsole().println(SERVER_CONN_UNAVAIL);
            }
            else {
                long start = System.currentTimeMillis();
                long end = start + TIMEOUT;

                try {
                    getConnector().disconnect();
                }
                catch ( ServerConnectorException e ) {
                    getLog().warn(SERVER_DISCONN_FAIL, e);
                    this.session.getConsole().println(SERVER_DISCONN_FAIL);
                }

                while ( System.currentTimeMillis() < end
                        && ! ( getConnector().getState() == ServerConnectorState.ERROR || getConnector().getState() == ServerConnectorState.DISCONNECTED ) ) {
                    Thread.sleep(100);
                }

                this.session.getConsole().print(
                    Ansi.ansi().bold().a(STATE).fg(getStatusColor(getConnector().getState())).a(getConnector().getState()).fg(Color.DEFAULT)
                            .boldOff().newline());

            }

            return null;
        }
    }

}
