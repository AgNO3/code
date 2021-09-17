/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class InputPump extends Thread {

    private static final Logger log = Logger.getLogger(InputPump.class);

    private boolean exit = false;
    private InputProvider h;
    private WritableByteChannel channel;


    /**
     * @param stdin
     * @param h
     */
    public InputPump ( OutputStream stdin, InputProvider h ) {
        super("InputPump"); //$NON-NLS-1$
        this.h = h;
        this.channel = Channels.newChannel(stdin);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run () {
        log.trace("Starting input pump"); //$NON-NLS-1$

        long written = 0;
        this.h.doStart();

        while ( !this.exit ) {
            try {
                if ( this.h.eof() ) {
                    break;
                }

                ByteBuffer buf = this.h.getInput();

                while ( buf.remaining() > 0 ) {
                    written += this.channel.write(buf);
                }
            }
            catch ( IOException e ) {
                log.warn("Failed to write input:", e); //$NON-NLS-1$
            }
            catch ( InterruptedException e ) {
                log.trace("Input provider was interrupted:", e); //$NON-NLS-1$
                break;
            }
        }

        this.h.doEnd();
        try {
            this.channel.close();
        }
        catch ( IOException e ) {
            log.warn("Failed to close input stream", e); //$NON-NLS-1$
        }

        if ( log.isTraceEnabled() ) {
            log.trace("Input pump finished " + written); //$NON-NLS-1$
        }
    }


    /**
     * 
     */
    public void shutdown () {
        if ( log.isTraceEnabled() ) {
            log.trace("Shutting down input pump"); //$NON-NLS-1$
        }
        this.exit = true;
        this.interrupt();
        try {
            this.join();
        }
        catch ( InterruptedException e ) {
            log.warn("Joining of input pump thread was interrupted:", e); //$NON-NLS-1$
        }
    }

}
