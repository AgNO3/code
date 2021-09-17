/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.02.2015 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.io.ObjectOutputStream;


/**
 * Closes the stream after the first object write
 * 
 * @author mbechler
 *
 */
public class ClosingObjectOutputStreamWrapper extends ObjectOutputStream {

    private ObjectOutputStream oos;


    /**
     * @param oos
     * @throws IOException
     */
    public ClosingObjectOutputStreamWrapper ( ObjectOutputStream oos ) throws IOException {
        super();
        this.oos = oos;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.ObjectOutputStream#writeObjectOverride(java.lang.Object)
     */
    @Override
    protected void writeObjectOverride ( Object obj ) throws IOException {
        this.oos.writeObject(obj);
        this.oos.close();
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.ObjectOutputStream#close()
     */
    @Override
    public void close () throws IOException {}


    /**
     * {@inheritDoc}
     *
     * @see java.io.ObjectOutputStream#flush()
     */
    @Override
    public void flush () throws IOException {}
}
