/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import org.apache.felix.service.command.CommandProcessor;
import org.apache.sshd.server.command.Command;
import org.apache.sshd.server.command.CommandFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    CommandFactory.class
} )
public class ConsoleCommandFactory implements CommandFactory {

    private CommandProcessor commandProcessor;


    @Reference
    protected synchronized void setCommandProcessor ( CommandProcessor proc ) {
        this.commandProcessor = proc;
    }


    protected synchronized void unsetCommandProcessor ( CommandProcessor proc ) {
        if ( this.commandProcessor == proc ) {
            this.commandProcessor = null;
        }
    }


    @Override
    public Command createCommand ( final String cmd ) {
        return new ConsoleCommandWrapper(this.commandProcessor, cmd);
    }
}
