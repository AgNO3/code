/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.07.2013 by mbechler
 */
package eu.agno3.runtime.util.log;


import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
public class LogWriter extends Writer {

    private final Logger log;
    private StringBuilder buf = new StringBuilder();
    private final Level level;


    /**
     * @param log
     * @param level
     */
    public LogWriter ( Logger log, Level level ) {
        this.log = log;
        this.level = level;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.io.Writer#close()
     */
    @Override
    public void close () throws IOException {
        this.flush();
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.io.Writer#flush()
     */
    @Override
    public void flush () throws IOException {
        if ( !this.log.isEnabledFor(this.level) ) {
            return;
        }
        String rest = this.buf.toString();

        if ( !rest.isEmpty() ) {
            this.log.log(this.level, rest);
            this.buf = new StringBuilder();
        }
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.io.Writer#write(char[], int, int)
     */
    @Override
    public void write ( char[] data, int off, int len ) throws IOException {
        if ( !this.log.isEnabledFor(this.level) ) {
            return;
        }
        if ( shouldIgnore(data, off, len) ) {
            return;
        }
        this.buf.append(data, off, len);

        int lastPos = 0;
        int pos = -1;
        while ( ( pos = this.buf.indexOf("\n", lastPos) ) >= 0 ) { //$NON-NLS-1$
            String line = this.buf.substring(lastPos, pos);
            if ( !ignoreMessage(line) ) {
                this.log.log(getMessageLevel(line), processMessage(line));
            }
            lastPos = pos + 1;
        }
        if ( lastPos > 0 ) {
            this.buf = new StringBuilder(this.buf.substring(lastPos));
        }
    }


    /**
     * @param line
     * @return
     */
    protected Level getMessageLevel ( String line ) {
        return this.level;
    }


    /**
     * @param line
     * @return
     */
    protected String processMessage ( String line ) {
        return line;
    }


    /**
     * @param line
     * @return
     */
    protected boolean ignoreMessage ( String line ) {
        return false;
    }


    /**
     * @param data
     * @param off
     * @param len
     * @return
     */
    protected boolean shouldIgnore ( char[] data, int off, int len ) {
        return false;
    }


    /**
     * @param log
     * @param level
     * @return a log wrapper which redirects the print writer output to a logger
     */
    public static PrintWriter createWriter ( Logger log, Level level ) {
        LogWriter wrapper = new LogWriter(log, level);
        return new PrintWriter(wrapper);
    }

}
