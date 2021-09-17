/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec;


import java.nio.CharBuffer;

import org.apache.commons.lang3.StringUtils;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractLineBufferingOutputHandler extends AbstractCharsetDecodingOutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -4742514226817913670L;


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler#output(java.nio.CharBuffer)
     */
    @Override
    protected void output ( CharBuffer buf, boolean eof ) {
        int lastNewline = -1;
        for ( int i = buf.remaining(); i > 0; i-- ) {
            if ( buf.charAt(i - 1) == '\n' ) {
                lastNewline = i;
            }
        }

        if ( eof ) {
            lastNewline = buf.remaining();
        }

        if ( lastNewline >= 0 ) {
            char[] tmp = new char[lastNewline];
            buf.get(tmp, 0, lastNewline - 1);
            buf.get();

            String str = new String(tmp);

            String[] lines = StringUtils.split(str, '\n');

            for ( String line : lines ) {
                this.outputLine(line);
            }

            buf.compact();
        }

    }


    /**
     * @param line
     */
    protected abstract void outputLine ( String line );

}
