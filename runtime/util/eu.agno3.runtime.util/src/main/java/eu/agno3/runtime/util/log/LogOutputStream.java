/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.util.log;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.apache.commons.io.output.NullOutputStream;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * @todo This ignores charset
 */

public class LogOutputStream extends OutputStream {

    /**
     * 
     */
    private static final String INTERNAL_ENCODING = "ISO-8859-1"; //$NON-NLS-1$

    private final Logger log;
    private StringBuilder buf = new StringBuilder();
    private Level level;
    private Charset c;


    /**
     * @param log
     * @param level
     * @param c
     *            charset for decoding the written data
     */
    public LogOutputStream ( Logger log, Level level, Charset c ) {
        this.log = log;
        this.level = level;
        this.c = c;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write ( int w ) throws IOException {
        this.buf.append((char) w);

        int pos = -1;
        while ( ( pos = this.buf.indexOf("\n") ) >= 0 ) { //$NON-NLS-1$
            String line = this.buf.substring(0, pos);
            this.log.log(this.level, line);
            this.buf.delete(0, pos + 1);
        }
    }


    protected Charset getCharset () {
        return this.c;
    }


    /**
     * @param log
     * @param level
     * @return a print stream redirected to the given log
     */
    public static PrintStream makePrintStream ( Logger log, Level level ) {

        try {
            Charset c = Charset.forName(INTERNAL_ENCODING);
            return new PrintStream(new LogOutputStream(log, level, c), true, c.name());
        }
        catch ( UnsupportedEncodingException e ) {
            log.error("Failed to setup LogOutputStream:", e); //$NON-NLS-1$
            return new PrintStream(new NullOutputStream());
        }
    }
}
