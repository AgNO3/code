/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.agent;


import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import eu.agno3.orchestrator.system.base.units.exec.InputProvider;


/**
 * @author mbechler
 *
 */
public class ChpasswdInputProvider implements InputProvider {

    /**
     * 
     */
    private static final long serialVersionUID = -3175916806707785225L;
    /**
     * 
     */
    private static final Charset CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$
    private transient ByteBuffer buffer;
    private byte[] bytes;


    /**
     * @param user
     * @param password
     */
    public ChpasswdInputProvider ( String user, String password ) {
        this.bytes = String.format("%s:%s\n", user, password).getBytes(CHARSET); //$NON-NLS-1$
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#doStart()
     */
    @Override
    public void doStart () {
        // NOP
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#doEnd()
     */
    @Override
    public void doEnd () {
        // NOP
    }


    /**
     * @return
     */
    private ByteBuffer getBuffer () {
        if ( this.buffer == null ) {
            this.buffer = ByteBuffer.wrap(this.bytes);
        }
        return this.buffer;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#eof()
     */
    @Override
    public boolean eof () {
        return getBuffer().remaining() == 0;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.exec.InputProvider#getInput()
     */
    @Override
    public ByteBuffer getInput () throws InterruptedException {
        return getBuffer();
    }

}
