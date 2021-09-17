/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 17.10.2016 by mbechler
 */
package eu.agno3.runtime.jmx.internal;


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.ObjectFactoryBuilder;

import org.apache.log4j.Logger;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;


/**
 * 
 * This disabled the JNDI reference mechanism which can be used to trigger remote classloading
 * 
 * @author mbechler
 *
 */
@Component ( immediate = true )
public class SecurityInitializer {

    private static final Logger log = Logger.getLogger(SecurityInitializer.class);


    private static void initialize () throws NamingException {
        NamingManager.setObjectFactoryBuilder(new NoRefererenceObjectFactoryBuilder());
    }


    @Activate
    protected synchronized void activate ( ComponentContext ctx ) {
        try {
            initialize();
        }
        catch ( Exception e ) {
            log.error("Failed to disable JNDI references", e); //$NON-NLS-1$
        }
    }

    private static class NoRefererenceObjectFactoryBuilder implements ObjectFactoryBuilder {

        private final ObjectFactory objectFactory;


        /**
         * 
         */
        public NoRefererenceObjectFactoryBuilder () {
            this.objectFactory = new NoReferenceObjectFactory();
        }


        /**
         * {@inheritDoc}
         *
         * @see javax.naming.spi.ObjectFactoryBuilder#createObjectFactory(java.lang.Object, java.util.Hashtable)
         */
        @Override
        public ObjectFactory createObjectFactory ( Object obj, Hashtable<?, ?> environment ) throws NamingException {
            return this.objectFactory;
        }

    }

    private static class NoReferenceObjectFactory implements ObjectFactory {

        /**
         * 
         */
        public NoReferenceObjectFactory () {}


        /**
         * {@inheritDoc}
         *
         * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name,
         *      javax.naming.Context, java.util.Hashtable)
         */
        @Override
        public Object getObjectInstance ( Object refInfo, Name name, Context nameCtx, Hashtable<?, ?> environment ) throws Exception {
            if ( refInfo instanceof Reference || refInfo instanceof Referenceable ) {
                throw new NamingException("Reference loading is disabled"); //$NON-NLS-1$
            }
            return refInfo;
        }

    }
}
