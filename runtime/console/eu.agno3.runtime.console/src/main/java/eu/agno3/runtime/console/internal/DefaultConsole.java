/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 21.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.io.IOException;

import org.apache.log4j.Logger;
import org.jline.terminal.TerminalBuilder;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.console.Console;
import eu.agno3.runtime.console.ConsoleFactory;
import eu.agno3.runtime.console.ShutdownHandler;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DefaultConsole.class
}, immediate = true, enabled = true )
public class DefaultConsole {

    private static final Logger log = Logger.getLogger(DefaultConsole.class);
    private static final String DEFAULT_CONSOLE_ENABLE_PROP = "console.launchDefault"; //$NON-NLS-1$

    private ConsoleFactory consoleFactory;
    private Console console;

    private ShutdownHandler shutdownHandler;


    @Reference
    protected synchronized void setConsoleFactory ( ConsoleFactory factory ) {
        this.consoleFactory = factory;
    }


    protected synchronized void unsetConsoleFactory ( ConsoleFactory factory ) {
        if ( this.consoleFactory == factory ) {
            this.consoleFactory = null;
        }
    }


    @Reference
    protected synchronized void setShutdownHandler ( ShutdownHandler handler ) {
        this.shutdownHandler = handler;
    }


    protected synchronized void unsetShutdownHandler ( ShutdownHandler handler ) {
        if ( this.shutdownHandler == handler ) {
            this.shutdownHandler = null;
        }
    }


    @Activate
    protected void activate ( ComponentContext context ) {
        if ( System.getProperty(DEFAULT_CONSOLE_ENABLE_PROP, Boolean.FALSE.toString()).equals(Boolean.TRUE.toString()) ) {
            try {
                this.console = this.consoleFactory.createConsole(TerminalBuilder.builder().system(true).build());
            }
            catch ( IOException e ) {
                log.error("Failed to create default console", e); //$NON-NLS-1$
            }
            this.console.setShutdownHandler(this.shutdownHandler);
            this.console.start();
        }
    }


    @Deactivate
    protected void deactivate ( ComponentContext context ) {
        if ( this.console != null ) {
            this.console.exit();
        }
    }
}
