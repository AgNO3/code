/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: Sep 30, 2016 by mbechler
 */
package eu.agno3.runtime.db.schema.orm.autoapply.internal;


import java.io.PrintStream;


/**
 * @author mbechler
 *
 */
public class NoClosePrintStream extends PrintStream {

    /**
     * @param console
     */
    public NoClosePrintStream ( PrintStream console ) {
        super(console, true);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.PrintStream#close()
     */
    @Override
    public void close () {
        // don't close delegate
    }
}
