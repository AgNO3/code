/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.io.PrintStream;

import org.jline.terminal.Terminal;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import eu.agno3.runtime.console.Console;
import eu.agno3.runtime.console.ConsoleConfiguration;
import eu.agno3.runtime.console.ConsoleFactory;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    ConsoleFactory.class
} )
public class ConsoleFactoryImpl implements ConsoleFactory {

    private DynamicCommandProcessor commandProcessor;
    private ConsoleConfiguration consoleConfiguration = new DefaultConsoleConfiguration();


    @Reference
    protected synchronized void setCommandProcessor ( DynamicCommandProcessor proc ) {
        this.commandProcessor = proc;
    }


    protected synchronized void unsetCommandProcessor ( DynamicCommandProcessor proc ) {
        if ( this.commandProcessor == proc ) {
            this.commandProcessor = null;
        }
    }


    @Reference ( policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY )
    protected synchronized void setConsoleConfiguration ( ConsoleConfiguration config ) {
        this.consoleConfiguration = config;
    }


    protected synchronized void unsetConsoleConfiguration ( ConsoleConfiguration config ) {
        if ( this.consoleConfiguration == config ) {
            this.consoleConfiguration = new DefaultConsoleConfiguration();
        }
    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.console.ConsoleFactory#createConsole(org.jline.terminal.Terminal)
     */
    @Override
    public Console createConsole ( Terminal term ) {

        return new ConsoleRunner(
            this.consoleConfiguration,
            this.commandProcessor.create(term.input(), new PrintStream(term.output()), new PrintStream(term.output())), // ,
                                                                                                                        // null,
            // term.getOutputEncoding(),
            // null)
            this.commandProcessor,
            term.getName(),
            term);
    }
}
