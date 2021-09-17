/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.nio.ByteBuffer;


/**
 * @author mbechler
 * 
 */
public class EOFInputProvider implements InputProvider {

    /**
     * 
     */
    private static final long serialVersionUID = -7886775121147550217L;
    /**
     * 
     */
    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#doStart()
     */
    @Override
    public void doStart () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#doEnd()
     */
    @Override
    public void doEnd () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#eof()
     */
    @Override
    public boolean eof () {
        return true;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#getInput()
     */
    @Override
    public ByteBuffer getInput () throws InterruptedException {
        return EMPTY_BUFFER;
    }

}
