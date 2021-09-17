/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.06.2013 by mbechler
 */
package eu.agno3.runtime.console.internal;


import java.io.InputStream;
import java.io.PrintStream;

import org.apache.felix.gogo.runtime.CommandProcessorImpl;
import org.apache.felix.gogo.runtime.threadio.ThreadIOImpl;
import org.apache.felix.service.command.CommandProcessor;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.console.Command;
import org.apache.karaf.shell.api.console.Registry;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.api.console.SessionFactory;
import org.apache.karaf.shell.api.console.Terminal;
import org.apache.karaf.shell.impl.action.command.ActionCommand;
import org.apache.karaf.shell.impl.console.CommandWrapper;
import org.apache.karaf.shell.impl.console.ConsoleSessionImpl;
import org.apache.karaf.shell.impl.console.HeadlessSessionImpl;
import org.apache.log4j.Logger;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import eu.agno3.runtime.console.CommandProvider;


/**
 * @author mbechler
 * 
 */
@Component ( service = {
    DynamicCommandProcessor.class, CommandProcessor.class
}, immediate = true )
public class DynamicCommandProcessor extends CommandProcessorImpl implements SessionFactory {

    private static final Logger log = Logger.getLogger(DynamicCommandProcessor.class);

    private Registry registry;
    private Registry dependencies = new ServiceRegistry();


    /**
     * 
     */
    public DynamicCommandProcessor () {
        super(new ThreadIOImpl());
    }


    @Reference
    protected synchronized void setServiceRegistry ( ServiceRegistry reg ) {
        this.registry = reg;
    }


    protected synchronized void unsetServiceRegistry ( ServiceRegistry reg ) {
        if ( this.registry == reg ) {
            this.registry = null;
        }
    }


    @SuppressWarnings ( "unchecked" )
    @Reference ( cardinality = ReferenceCardinality.MULTIPLE )
    protected synchronized void bindCommandProvider ( final CommandProvider prov ) {
        log.info("Adding commands of class " + prov.getClass().getName()); //$NON-NLS-1$
        for ( final Class<?> subclass : prov.getClass().getClasses() ) {

            if ( Action.class.isAssignableFrom(subclass) ) {
                org.apache.karaf.shell.api.action.Command cmd = subclass.getAnnotation(org.apache.karaf.shell.api.action.Command.class);
                if ( cmd != null ) {
                    ActionCommand c = new ActionCommand(
                        new ExtendedManagerImpl(prov, this.dependencies, this.registry, true),
                        (Class<? extends Action>) subclass);
                    this.registry.register(c);
                    addCommand(cmd.scope(), new CommandWrapper(c), cmd.name());
                }
            }
        }

        log.trace("Available commands:"); //$NON-NLS-1$
        for ( String command : getCommands() ) {
            log.trace(" - " + command); //$NON-NLS-1$
        }
    }


    protected synchronized void unbindCommandProvider ( CommandProvider service ) {
        log.debug("Removing commands of class " + service.getClass().getName()); //$NON-NLS-1$

        for ( final Class<?> subclass : service.getClass().getClasses() ) {

            org.apache.karaf.shell.api.action.Command cmd = subclass.getAnnotation(org.apache.karaf.shell.api.action.Command.class);

            if ( !subclass.getDeclaringClass().equals(service.getClass()) || !Action.class.isAssignableFrom(subclass) || !subclass.isMemberClass()
                    || cmd == null ) {
                continue;
            }

            Command c = this.registry.getCommand(cmd.scope(), cmd.name());
            if ( c != null ) {
                this.registry.unregister(c);
            }

            removeCommand(cmd.scope(), cmd.name());
        }
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.SessionFactory#create(java.io.InputStream, java.io.PrintStream,
     *      java.io.PrintStream)
     */
    @Override
    public Session create ( InputStream in, PrintStream out, PrintStream err ) {
        return create(in, out, err, null);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.SessionFactory#create(java.io.InputStream, java.io.PrintStream,
     *      java.io.PrintStream, org.apache.karaf.shell.api.console.Session)
     */
    @Override
    public Session create ( InputStream in, PrintStream out, PrintStream err, Session parent ) {
        return new HeadlessSessionImpl(this, this, in, out, err, parent);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.SessionFactory#create(java.io.InputStream, java.io.PrintStream,
     *      java.io.PrintStream, org.apache.karaf.shell.api.console.Terminal, java.lang.String, java.lang.Runnable)
     */
    @Override
    public Session create ( InputStream in, PrintStream out, PrintStream err, Terminal term, String encoding, Runnable closeCallback ) {
        return new ConsoleSessionImpl(this, this, this.threadIO, in, out, err, term, encoding, closeCallback);
    }


    /**
     * {@inheritDoc}
     *
     * @see org.apache.karaf.shell.api.console.SessionFactory#getRegistry()
     */
    @Override
    public Registry getRegistry () {
        return this.registry;
    }
}
