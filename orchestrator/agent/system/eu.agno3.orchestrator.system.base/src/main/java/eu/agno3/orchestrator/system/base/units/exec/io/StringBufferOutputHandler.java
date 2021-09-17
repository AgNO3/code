/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.05.2014 by mbechler
 */
package eu.agno3.orchestrator.system.base.units.exec.io;


import java.nio.CharBuffer;

import eu.agno3.orchestrator.system.base.units.exec.AbstractCharsetDecodingOutputHandler;


/**
 * @author mbechler
 * 
 */
public class StringBufferOutputHandler extends AbstractCharsetDecodingOutputHandler {

    /**
     * 
     */
    private static final long serialVersionUID = -4447074741344201593L;
    private StringBuffer strBuf = new StringBuffer();


    /**
     * @param cb
     */
    @Override
    protected void output ( CharBuffer cb, boolean eof ) {
        this.strBuf.append(cb);
        cb.clear();
    }


    /**
     * @return the contents of the produced string buffer
     */
    public String getString () {
        return this.strBuf.toString();
    }
}
