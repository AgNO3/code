/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class OutputPump extends Thread {

    private static final Logger log = Logger.getLogger(OutputPump.class);

    private static final int BUFFER_SIZE = 4096;

    private boolean exit = false;
    private OutputHandler h;
    private ReadableByteChannel channel;

    private String streamName;


    /**
     * @param streamName
     * @param stream
     * @param h
     */
    public OutputPump ( String streamName, InputStream stream, OutputHandler h ) {
        super(String.format("OutputPump for %s", streamName)); //$NON-NLS-1$
        this.streamName = streamName;
        this.h = h;
        this.channel = Channels.newChannel(stream);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.Thread#run()
     */
    @Override
    public void run () {
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);

        if ( log.isTraceEnabled() ) {
            log.trace("Starting output pump for " + this.streamName); //$NON-NLS-1$
        }

        while ( !this.exit ) {

            try {
                int readLocal = this.channel.read(buf);

                if ( readLocal == -1 ) {
                    break;
                }

                doBuffer(buf);

            }
            catch ( ClosedByInterruptException e ) {
                log.debug("Stream read interrupted:", e); //$NON-NLS-1$
                break;
            }
            catch ( IOException e ) {
                log.warn("Stream read failed:", e); //$NON-NLS-1$
                break;
            }

        }

        buf.flip();
        this.h.eof(buf);

        if ( log.isTraceEnabled() ) {
            log.trace("Exiting output pump for " + this.streamName); //$NON-NLS-1$
        }
    }


    private void doBuffer ( ByteBuffer buf ) {
        buf.flip();

        if ( buf.remaining() > 0 ) {
            try {
                this.h.output(buf);
            }
            catch ( Exception e ) {
                log.warn("Output hander threw exception:", e); //$NON-NLS-1$
            }
        }

        buf.flip();
    }


    /**
     * @param force
     * 
     */
    public void shutdown ( boolean force ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Shutting down output pump for " + this.streamName); //$NON-NLS-1$
        }
        if ( force ) {
            this.exit = true;
            this.interrupt();
        }
        try {
            this.join();
        }
        catch ( InterruptedException e ) {
            log.warn("Joining of output pump thread was interrupted:", e); //$NON-NLS-1$
        }
    }

}
