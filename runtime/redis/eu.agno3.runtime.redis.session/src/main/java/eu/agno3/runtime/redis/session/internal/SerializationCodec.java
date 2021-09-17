package eu.agno3.runtime.redis.session.internal;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.session.SessionContext;
import org.redisson.client.codec.Codec;
import org.redisson.client.handler.State;
import org.redisson.client.protocol.Decoder;
import org.redisson.client.protocol.Encoder;

import eu.agno3.runtime.util.serialization.UnsafeObjectInputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;


/**
 * 
 * @author mbechler
 *
 */
public class SerializationCodec implements Codec {

    private static final Logger log = Logger.getLogger(SerializationCodec.class);

    private final Decoder<Object> decoder = new Decoder<Object>() {

        @Override
        public Object decode ( ByteBuf buf, State state ) throws IOException {
            try ( ByteBufInputStream in = new ByteBufInputStream(buf) ) {

                final AtomicReference<Object> reference = new AtomicReference<>();
                final AtomicReference<Exception> exception = new AtomicReference<>();

                getContext().run( () -> {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    try ( ObjectInputStream inputStream = new UnsafeObjectInputStream(in, cl) ) {
                        reference.set(inputStream.readObject());
                    }
                    catch ( Exception e ) {
                        exception.set(e);
                    }
                });

                if ( exception.get() != null ) {
                    throw exception.get();
                }

                return reference.get();
            }
            catch ( IOException e ) {
                throw e;
            }
            catch ( Exception e ) {
                throw new IOException(e);
            }
        }
    };

    private final Encoder encoder = new Encoder() {

        @Override
        public ByteBuf encode ( Object in ) throws IOException {
            ByteBuf out = ByteBufAllocator.DEFAULT.buffer();
            try ( ByteBufOutputStream result = new ByteBufOutputStream(out);
                  ObjectOutputStream outputStream = new ObjectOutputStream(result) ) {
                outputStream.writeObject(in);
                outputStream.close();
                if ( getLog().isDebugEnabled() ) {
                    getLog().debug("Session size is " + result.writtenBytes()); //$NON-NLS-1$
                }
                return result.buffer();
            }
            catch ( IOException e ) {
                out.release();
                throw e;
            }
        }
    };

    private final SessionContext context;


    final SessionContext getContext () {
        return this.context;
    }


    /**
     * @return the log
     */
    final static Logger getLog () {
        return log;
    }


    /**
     * 
     * @param context
     */
    public SerializationCodec ( SessionContext context ) {
        this.context = context;
    }


    @Override
    public Decoder<Object> getMapValueDecoder () {
        return getValueDecoder();
    }


    @Override
    public Encoder getMapValueEncoder () {
        return getValueEncoder();
    }


    @Override
    public Decoder<Object> getMapKeyDecoder () {
        return getValueDecoder();
    }


    @Override
    public Encoder getMapKeyEncoder () {
        return getValueEncoder();
    }


    @Override
    public Decoder<Object> getValueDecoder () {
        return this.decoder;
    }


    @Override
    public Encoder getValueEncoder () {
        return this.encoder;
    }

}