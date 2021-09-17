/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.06.2013 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import org.apache.sshd.common.Factory;
import org.apache.sshd.server.command.Command;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.ConsoleFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ShellFactory.class
} )
public class ShellFactory implements Factory<Command> {

    private ConsoleFactory consoleFactory;


    @Reference
    protected synchronized void setConsoleFactory ( ConsoleFactory cf ) {
        this.consoleFactory = cf;
    }


    protected synchronized void unsetConsoleFactory ( ConsoleFactory cf ) {
        if ( this.consoleFactory == cf ) {
            this.consoleFactory = null;
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.sshd.common.Factory#create()
     */
    @Override
    public Command create () {
        return new KarafShell(this.consoleFactory);
    }
}
