/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.util.Collection;
import java.util.Collections;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.console.Session;
import org.fusesource.jansi.Ansi;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;

import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
public class HelpCommandProvider implements CommandProvider {

    private DynamicCommandProcessor commandProcessor;


    @Reference ( policy = ReferencePolicy.DYNAMIC )
    protected synchronized void setDynamicCommandProcessor ( DynamicCommandProcessor proc ) {
        this.commandProcessor = proc;
    }


    protected synchronized void unsetDynamicCommandProcessor ( DynamicCommandProcessor proc ) {
        if ( this.commandProcessor == proc ) {
            this.commandProcessor = null;
        }
    }


    /**
     * @return the commandProcessor
     */
    synchronized DynamicCommandProcessor getCommandProcessor () {
        return this.commandProcessor;
    }

    /**
     * Display command help
     * 
     * @author mbechler
     * 
     */
    @Command ( scope = "*", name = "help", description = "Display command help" )
    public class HelpCommand implements Action {

        @Argument ( name = "command", required = false, description = "The command to get help for" )
        private String command = null;

        @org.apache.karaf.shell.api.action.lifecycle.Reference
        private Session session;


        @Override
        public Object execute () {
            DynamicCommandProcessor cp = getCommandProcessor();
            if ( cp != null && this.command == null ) {
                this.session.getConsole().println(Ansi.ansi().bold().a("Commands:").boldOff()); //$NON-NLS-1$
                for ( String availCommand : cp.getCommands() ) {
                    this.session.getConsole().println(availCommand);
                }
            }
            else {
                this.session.getConsole().println("No help available"); //$NON-NLS-1$
            }
            return null;
        }


        /**
         * @return list of commands
         */
        public Collection<String> complete () {
            DynamicCommandProcessor cp = getCommandProcessor();
            if ( cp != null ) {
                return cp.getCommands();
            }
            return Collections.EMPTY_LIST;
        }

    }

}
