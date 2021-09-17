/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.06.2013 by mbechler
 */
package eu.agno3.runtime.console.osgi.internal;


import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;

import org.apache.felix.service.command.CommandSession;
import org.eclipse.osgi.framework.console.CommandInterpreter;
import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public class CommandSessionBridge implements CommandInterpreter {

    private CommandSession backend;
    private Iterator<String> args;


    /**
     * @param backend
     * @param args
     */
    CommandSessionBridge ( CommandSession backend, List<String> args ) {
        super();
        this.backend = backend;
        this.args = args.iterator();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#nextArgument()
     */
    @Override
    public String nextArgument () {
        if ( this.args.hasNext() ) {
            return this.args.next();
        }
        return null;
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#execute(java.lang.String)
     */
    @Override
    public Object execute ( String cmd ) {
        try {
            return this.backend.execute(cmd);
        }
        catch ( Exception e ) {
            throw new ConsoleRuntimeException(String.format("Failed to execute console command '%s'", cmd), e); //$NON-NLS-1$
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#print(java.lang.Object)
     */
    @Override
    public void print ( Object o ) {
        this.backend.getConsole().print(o.toString());
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#println()
     */
    @Override
    public void println () {
        this.backend.getConsole().println();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#println(java.lang.Object)
     */
    @Override
    public void println ( Object o ) {
        this.backend.getConsole().println(o.toString());
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#printStackTrace(java.lang.Throwable)
     */
    @Override
    public void printStackTrace ( Throwable t ) {
        t.printStackTrace(this.backend.getConsole());
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#printDictionary(java.util.Dictionary,
     *      java.lang.String)
     */
    @Override
    public void printDictionary ( Dictionary<?, ?> dic, String title ) {
        throw new UnsupportedOperationException();
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.eclipse.osgi.framework.console.CommandInterpreter#printBundleResource(org.osgi.framework.Bundle,
     *      java.lang.String)
     */
    @Override
    public void printBundleResource ( Bundle bundle, String resource ) {
        throw new UnsupportedOperationException();
    }

}
