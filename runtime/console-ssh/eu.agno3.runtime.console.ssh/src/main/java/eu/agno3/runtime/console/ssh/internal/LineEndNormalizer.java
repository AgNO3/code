/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 15.01.2014 by mbechler
 */
package eu.agno3.runtime.console.ssh.internal;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * Translates UNIX (NL) line endings to CR-NL.
 * 
 * @author mbechler
 * 
 */
class LineEndNormalizer extends FilterOutputStream {

    private int prevChar = -1;


    /**
     * 
     * @param out
     */
    public LineEndNormalizer ( OutputStream out ) {
        super(out);
    }


    @Override
    public void write ( int b ) throws IOException {
        if ( b == '\n' && this.prevChar != '\r' ) {
            super.write('\r');
            super.write('\n');

        }
        else {
            super.write(b);
        }

        this.prevChar = b;
    }
}