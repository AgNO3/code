/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 26.08.2013 by mbechler
 */
package eu.agno3.runtime.util.classloading;


import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public class BundleDelegatingClassLoader extends ClassLoader {

    private Bundle bundle;


    /**
     * @param b
     */
    public BundleDelegatingClassLoader ( Bundle b ) {
        this.bundle = b;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        return this.bundle.loadClass(name);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals ( Object obj ) {

        if ( obj instanceof BundleDelegatingClassLoader ) {
            return this.bundle.equals( ( (BundleDelegatingClassLoader) obj ).bundle);
        }

        return super.equals(obj);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode () {
        return this.bundle.hashCode();
    }
}
