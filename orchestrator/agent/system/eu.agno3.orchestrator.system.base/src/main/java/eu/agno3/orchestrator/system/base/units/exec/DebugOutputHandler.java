/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.12.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.nio.CharBuffer;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class DebugOutputHandler extends AbstractCharsetDecodingOutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = 254774670467858474L;

    private transient Logger log;
    private String loggerName;


    /**
     * @param log
     * 
     */
    public DebugOutputHandler ( Logger log ) {
        this.log = log;
        this.loggerName = log.getName();
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler#output(java.nio.CharBuffer,
     *      boolean)
     */
    @Override
    protected void output ( CharBuffer buf, boolean eof ) {
        getLogger().debug(buf);
    }


    /**
     * @return
     */
    private Logger getLogger () {
        if ( this.log == null ) {
            this.log = Logger.getLogger(this.loggerName);
        }
        return this.log;
    }

}
