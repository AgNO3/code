/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 09.11.2015 by mbechler
 */
package eu.agno3.runtime.util.serialization;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;


/**
 * @author mbechler
 *
 */
public class FilteredObjectInputStream extends ObjectInputStream {

    private ClassLoader cl;


    /**
     * @param in
     * @param cl
     * @throws IOException
     */
    public FilteredObjectInputStream ( InputStream in, ClassLoader cl ) throws IOException {
        super(in);
        this.cl = cl;
    }


    /**
     * {@inheritDoc}
     *
     * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
     */
    @Override
    protected Class<?> resolveClass ( ObjectStreamClass desc ) throws IOException, ClassNotFoundException {
        try {
            return SerializationFilter.checkAndLoadClass(desc.getName(), this.cl);
        }
        catch ( UnsafeSerializableException e ) {
            throw new IOException("Unsafe class in serialized data", e); //$NON-NLS-1$
        }
    }

}
