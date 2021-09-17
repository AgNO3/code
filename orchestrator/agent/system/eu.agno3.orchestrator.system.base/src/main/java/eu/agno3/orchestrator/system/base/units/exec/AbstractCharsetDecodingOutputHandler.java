/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractCharsetDecodingOutputHandler implements OutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 2602751953626133376L;

    private static final int BUF_SIZE = 4096;
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8"); //$NON-NLS-1$
    private transient CharsetDecoder decoder;
    private transient CharBuffer cb;
    private transient Charset ch;
    private String charsetName;


    /**
     * @param ch
     */
    public AbstractCharsetDecodingOutputHandler ( Charset ch ) {
        this.ch = ch;
        this.charsetName = ch.name();
    }


    /**
     * 
     */
    public AbstractCharsetDecodingOutputHandler () {
        this(DEFAULT_CHARSET);
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.OutputHandler#output(java.nio.ByteBuffer)
     */
    @Override
    public synchronized void output ( ByteBuffer buf ) {
        getDecoder().decode(buf, getBuffer(), false);
        handleBuffer(false);
        buf.flip();
        buf.compact();
    }


    /**
     * @return
     */
    private CharBuffer getBuffer () {
        if ( this.cb == null ) {
            this.cb = CharBuffer.allocate(BUF_SIZE);
        }
        return this.cb;
    }


    /**
     * @return
     */
    private CharsetDecoder getDecoder () {
        if ( this.decoder == null ) {
            this.decoder = getCharset().newDecoder();
        }
        return this.decoder;
    }


    /**
     * @return
     */
    private Charset getCharset () {
        if ( this.ch == null ) {
            this.ch = Charset.forName(this.charsetName);
        }
        return this.ch;
    }


    /**
     * @param buf
     */
    protected abstract void output ( CharBuffer buf, boolean eof );


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.OutputHandler#eof(java.nio.ByteBuffer)
     */
    @Override
    public void eof ( ByteBuffer remaining ) {
        getDecoder().decode(remaining, getBuffer(), true);
        getDecoder().flush(getBuffer());
        handleBuffer(true);
        remaining.flip();
    }


    private void handleBuffer ( boolean eof ) {
        getBuffer().flip();
        if ( getBuffer().length() > 0 ) {
            this.output(getBuffer(), eof);
        }
    }

}