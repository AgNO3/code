/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.09.2015 by mbechler
 */
package eu.agno3.runtime.xml.binding.internal;


import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.persistence.internal.jaxb.JaxbClassLoader;

import eu.agno3.runtime.util.classloading.CompositeClassLoader;


/**
 * @author mbechler
 *
 */
public class JAXBBindingClassLoader extends CompositeClassLoader {

    private static final Logger log = Logger.getLogger(JaxbClassLoader.class);

    private BindingBundleTracker bindingTracker;


    /**
     * @param bt
     * @param classloaders
     */
    public JAXBBindingClassLoader ( BindingBundleTracker bt, Set<ClassLoader> classloaders ) {
        super(classloaders);
        this.bindingTracker = bt;
    }


    /**
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.util.classloading.CompositeClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass ( String name ) throws ClassNotFoundException {

        if ( name.contains(".jaxws_asm.") ) { //$NON-NLS-1$
            return super.loadClass(name);
        }

        try {
            ClassLoader globalContextClassLoader = this.bindingTracker.getGlobalContextClassLoader();
            if ( globalContextClassLoader == null ) {
                throw new IllegalStateException("Global classloader is null"); //$NON-NLS-1$
            }
            return globalContextClassLoader.loadClass(name);
        }
        catch ( ClassNotFoundException e ) {
            log.debug("Not found in global context " + name, e); //$NON-NLS-1$
        }

        return super.loadClass(name);
    }
}
