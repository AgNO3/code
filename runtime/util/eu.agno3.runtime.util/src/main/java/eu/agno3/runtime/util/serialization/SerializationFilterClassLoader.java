package eu.agno3.runtime.util.serialization;


/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 07.11.2015 by mbechler
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 *
 */
public class SerializationFilterClassLoader extends ClassLoader {

    private static final Logger log = Logger.getLogger(SerializationFilterClassLoader.class);

    private ClassLoader delegate;


    /**
     * @param delegate
     */
    public SerializationFilterClassLoader ( ClassLoader delegate ) {
        super(delegate);
        this.delegate = delegate;
    }


    @Override
    public int hashCode () {
        return this.delegate.hashCode();
    }


    @Override
    public boolean equals ( Object obj ) {
        return this.delegate.equals(obj);
    }


    @Override
    public String toString () {
        return this.delegate.toString();
    }


    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        log.debug("Trying to load class " + name); //$NON-NLS-1$
        try {
            return SerializationFilter.checkAndLoadClass(name, this.delegate);
        }
        catch ( UnsafeSerializableException e ) {
            throw new ClassNotFoundException("Rejected classload", e); //$NON-NLS-1$
        }
    }


    @Override
    public URL getResource ( String name ) {
        return this.delegate.getResource(name);
    }


    @Override
    public Enumeration<URL> getResources ( String name ) throws IOException {
        return this.delegate.getResources(name);
    }


    @Override
    public InputStream getResourceAsStream ( String name ) {
        return this.delegate.getResourceAsStream(name);
    }


    @Override
    public void setDefaultAssertionStatus ( boolean enabled ) {
        this.delegate.setDefaultAssertionStatus(enabled);
    }


    @Override
    public void setPackageAssertionStatus ( String packageName, boolean enabled ) {
        this.delegate.setPackageAssertionStatus(packageName, enabled);
    }


    @Override
    public void setClassAssertionStatus ( String className, boolean enabled ) {
        this.delegate.setClassAssertionStatus(className, enabled);
    }


    @Override
    public void clearAssertionStatus () {
        this.delegate.clearAssertionStatus();
    }

}
