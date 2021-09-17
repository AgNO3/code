/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.09.2013 by mbechler
 */
package eu.agno3.orchestrator.agent.server.console.internal;


import java.util.UUID;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Color;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.orchestrator.agent.server.AgentConnectorWatcher;
import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = CommandProvider.class )
public class AgentServerCommandProvider implements CommandProvider {

    private AgentConnectorWatcher agentWatcher;


    @Reference ( policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setAgentWatcher ( AgentConnectorWatcher watcher ) {
        this.agentWatcher = watcher;
    }


    protected synchronized void unsetAgentWatcher ( AgentConnectorWatcher watcher ) {
        if ( this.agentWatcher == watcher ) {
            this.agentWatcher = null;
        }
    }


    /**
     * @return the agentWatcher
     */
    synchronized AgentConnectorWatcher getAgentWatcher () {
        return this.agentWatcher;
    }

    /**
     * List registered agents and their states
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "agents", name = "list", description = "List registered agents and their states" )
    public class ListCommand implements Action {

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {

            if ( getAgentWatcher() == null ) {
                this.session.getConsole().println("Agent watcher not available"); //$NON-NLS-1$
            }

            Ansi out = Ansi.ansi();

            for ( UUID agentId : getAgentWatcher().getActiveComponentIds() ) {
                out.fg(Color.BLUE).a(agentId.toString()).fg(Color.DEFAULT).a(": ").boldOff(); //$NON-NLS-1$
                out.bold().a(getAgentWatcher().getComponentConnectorState(agentId).name()).boldOff();
                out.a(" last ping: ").a(getAgentWatcher().getLastPing(agentId).toString()); //$NON-NLS-1$
                out.newline();
            }

            this.session.getConsole().print(out.toString());

            return null;
        }
    }

}
