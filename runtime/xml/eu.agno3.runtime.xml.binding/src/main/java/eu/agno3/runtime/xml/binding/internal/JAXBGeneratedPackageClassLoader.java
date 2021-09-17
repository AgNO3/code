/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 04.09.2013 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collection;

import org.apache.log4j.Logger;


/**
 * @author mbechler
 * 
 */
class JAXBGeneratedPackageClassLoader extends ClassLoader {

    private static final String JAXB_INDEX_FILENAME = "/jaxb.index"; //$NON-NLS-1$
    private static final String PATH_SEPARATOR = "/"; //$NON-NLS-1$
    private static final String PACKAGE_SEPARATOR = "."; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(JAXBGeneratedPackageClassLoader.class);

    private final ClassLoader delegate;
    private final Collection<String> classes;
    private final String packageName;


    /**
     * @param packageName
     * @param classes
     * @param delegate
     * 
     */
    JAXBGeneratedPackageClassLoader ( String packageName, Collection<String> classes, ClassLoader delegate ) {
        this.delegate = delegate;
        this.classes = classes;
        this.packageName = packageName;
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {
        return this.delegate.loadClass(name);
    }


    /**
     * {@inheritDoc}
     * 
     * @see java.lang.ClassLoader#getResourceAsStream(java.lang.String)
     */
    @Override
    public InputStream getResourceAsStream ( String name ) {

        if ( isJAXBIndexFile(this.packageName, name) ) {

            InputStream delegateRes = this.delegate.getResourceAsStream(name);
            if ( delegateRes != null ) {
                return delegateRes;
            }

            log.debug("Return virtual JAXB Index"); //$NON-NLS-1$

            StringBuilder buf = new StringBuilder();

            for ( String className : this.classes ) {
                buf.append(className);
                buf.append(System.lineSeparator());
            }

            return new ByteArrayInputStream(buf.toString().getBytes(Charset.defaultCharset()));
        }
        return super.getResourceAsStream(name);
    }


    /**
     * @param name
     * @return
     */
    private static boolean isJAXBIndexFile ( String packageName, String name ) {
        return name.equals(packageToPath(packageName) + JAXB_INDEX_FILENAME);
    }


    /**
     * @return
     */
    private static String packageToPath ( String packageName ) {
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }


    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    // +GENERATED
    @Override
    public int hashCode () {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( this.delegate == null ) ? 0 : this.delegate.hashCode() );
        result = prime * result + ( ( this.packageName == null ) ? 0 : this.packageName.hashCode() );
        return result;
    }


    // -GENERATED

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    // +GENERATED
    @Override
    public boolean equals ( Object obj ) {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        JAXBGeneratedPackageClassLoader other = (JAXBGeneratedPackageClassLoader) obj;
        if ( this.delegate == null ) {
            if ( other.delegate != null )
                return false;
        }
        else if ( !this.delegate.equals(other.delegate) )
            return false;
        if ( this.packageName == null ) {
            if ( other.packageName != null )
                return false;
        }
        else if ( !this.packageName.equals(other.packageName) )
            return false;
        return true;
    }
    // -GENERATED

}
