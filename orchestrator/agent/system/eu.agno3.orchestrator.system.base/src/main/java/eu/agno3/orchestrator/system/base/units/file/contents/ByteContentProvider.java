/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.03.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.file.contents;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

import eu.agno3.orchestrator.system.base.execution.Context;
import eu.agno3.orchestrator.system.base.execution.exception.ExecutionException;


/**
 * @author mbechler
 * 
 */
public class ByteContentProvider implements ContentProvider {

    /**
     * 
     */
    private static final long serialVersionUID = -2296808320747034484L;
    private static final long DEFAULT_BUFFER_SIZE = 4096;
    private long bufferSize = DEFAULT_BUFFER_SIZE;
    private byte[] data;


    /**
     * @param data
     * @param bufferSize
     */
    public ByteContentProvider ( byte[] data, long bufferSize ) {
        this.data = Arrays.copyOf(data, data.length);
        this.bufferSize = bufferSize;
    }


    /**
     * @param data
     */
    public ByteContentProvider ( byte[] data ) {
        this.data = Arrays.copyOf(data, data.length);
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#validate(eu.agno3.orchestrator.system.base.execution.Context)
     */
    @Override
    public void validate ( Context ctx ) throws ExecutionException {

    }


    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.file.contents.ContentProvider#transferTo(eu.agno3.orchestrator.system.base.execution.Context,
     *      java.nio.channels.FileChannel)
     */
    @Override
    public void transferTo ( Context ctx, FileChannel c ) throws IOException {

        try ( ReadableByteChannel inputChannel = Channels.newChannel(new ByteArrayInputStream(this.data)) ) {
            while ( inputChannel.isOpen() ) {
                if ( c.transferFrom(inputChannel, c.position(), this.bufferSize) == 0 ) {
                    break;
                }
            }
        }
    }

}
