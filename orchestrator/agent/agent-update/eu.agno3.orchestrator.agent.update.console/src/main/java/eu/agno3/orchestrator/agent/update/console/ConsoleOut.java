/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.06.2016 by mbechler
 */
package eu.agno3.orchestrator.agent.update.console;

import java.io.PrintStream;

import eu.agno3.orchestrator.system.base.execution.output.Out;

/**
 * @author mbechler
 *
 */
final class ConsoleOut implements Out {

    private PrintStream ps;


    /**
     * @param ps 
     * 
     */
    public ConsoleOut ( PrintStream ps ) {
        this.ps = ps;
    }


    @Override
    public boolean isDebugEnabled () {
        return false;
    }


    @Override
    public void debug ( String msg ) {}


    @Override
    public void debug ( String msg, Throwable t ) {}


    @Override
    public void info ( String msg ) {
        this.ps.println(msg);
    }


    @Override
    public void info ( String msg, Throwable t ) {
        this.ps.println(msg);
        t.printStackTrace(this.ps);
    }


    @Override
    public void error ( String msg ) {
        this.ps.println(msg);
    }


    @Override
    public void error ( String msg, Throwable t ) {
        this.ps.println(msg);
        t.printStackTrace(this.ps);
    }


    @Override
    public Out getChild ( String name ) {
        return this;
    }
}