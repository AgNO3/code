/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.02.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.execution.output;


import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class OutLoggerBridge implements Out {

    private Logger backing;


    /**
     * 
     * @param backing
     */
    public OutLoggerBridge ( Logger backing ) {
        this.backing = backing;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#isDebugEnabled()
     */
    @Override
    public boolean isDebugEnabled () {
        return this.backing.isDebugEnabled();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#debug(java.lang.String)
     */
    @Override
    public void debug ( String msg ) {
        this.backing.debug(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#debug(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void debug ( String msg, Throwable t ) {
        this.backing.debug(msg, t);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#info(java.lang.String)
     */
    @Override
    public void info ( String msg ) {
        this.backing.info(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#info(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void info ( String msg, Throwable t ) {
        this.backing.info(msg, t);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#error(java.lang.String)
     */
    @Override
    public void error ( String msg ) {
        this.backing.error(msg);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#error(java.lang.String, java.lang.Throwable)
     */
    @Override
    public void error ( String msg, Throwable t ) {
        this.backing.error(msg, t);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.execution.output.Out#getChild(java.lang.String)
     */
    @Override
    public Out getChild ( String name ) {
        return new OutLoggerBridge(Logger.getLogger(this.backing.getName() + "." + name)); //$NON-NLS-1$
    }

}
