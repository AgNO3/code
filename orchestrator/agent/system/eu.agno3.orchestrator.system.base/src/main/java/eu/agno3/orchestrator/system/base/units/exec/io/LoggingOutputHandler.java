/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec.io;


import java.nio.CharBuffer;
import java.nio.charset.Charset;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler;


/**
 * @author mbechler
 * 
 */
public class LoggingOutputHandler extends AbstractCharsetDecodingOutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -266543633800983191L;
    private static final Logger log = Logger.getLogger(LoggingOutputHandler.class);
    private int prio;
    private transient Priority pr;


    /**
     * @param prio
     */
    public LoggingOutputHandler ( Priority prio ) {
        this.prio = prio.toInt();
    }


    /**
     * @param prio
     * @param charset
     */
    public LoggingOutputHandler ( Priority prio, Charset charset ) {
        super(charset);
        this.prio = prio.toInt();
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler#output(java.lang.String)
     */
    @Override
    protected void output ( CharBuffer cb, boolean eof ) {
        char[] t = new char[cb.remaining()];
        cb.get(t);
        cb.clear();
        String str = new String(t);
        str = str.trim();
        if ( !str.isEmpty() ) {
            log.log(getPriority(), str);
        }
    }


    /**
     * @return
     */
    private Priority getPriority () {
        if ( this.pr == null ) {
            this.pr = Level.toLevel(this.prio);
        }
        return this.pr;
    }
}
