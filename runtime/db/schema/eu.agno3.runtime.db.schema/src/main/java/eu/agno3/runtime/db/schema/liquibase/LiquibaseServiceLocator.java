/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.07.2013 by mbechler
 */
package eu.agno3.runtime.db.schema.liquibase;


import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import liquibase.Liquibase;
import liquibase.exception.ServiceNotFoundException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.LiquibaseService;
import liquibase.servicelocator.ServiceLocator;

import org.apache.log4j.Logger;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import eu.agno3.runtime.util.osgi.ResourceUtil;


/**
 * @author mbechler
 * 
 */
@Component ( service = LiquibaseServiceLocator.class )
public class LiquibaseServiceLocator extends ServiceLocator {

    private static final String CLASS_PATH = "/"; //$NON-NLS-1$
    private static final String CLASS_FILES = "*.class"; //$NON-NLS-1$

    private static final Logger log = Logger.getLogger(LiquibaseServiceLocator.class);
    private Index classIndex;
    private BundleContext bundleContext;
    private LogFactory logFactory;


    @Activate
    protected void activate ( ComponentContext context ) {
        this.bundleContext = context.getBundleContext();
        log.debug("Setting up service locator"); //$NON-NLS-1$
        ServiceLocator.setInstance(this);
        this.setResourceAccessor(new BundleResourceAccessor(FrameworkUtil.getBundle(Liquibase.class)));
        LogFactory.setInstance(this.logFactory);
        buildClassIndex();

    }


    @Reference
    protected synchronized void setLogFactory ( LogFactory lf ) {
        this.logFactory = lf;
    }


    protected synchronized void unsetLogFactory ( LogFactory lf ) {
        if ( this.logFactory == lf ) {
            this.logFactory = null;
        }
    }


    /**
     * 
     */
    private void buildClassIndex () {
        Bundle liquibaseBundle = FrameworkUtil.getBundle(Liquibase.class);
        List<URL> liquibaseClasses = ResourceUtil.safeFindPattern(liquibaseBundle, CLASS_PATH, CLASS_FILES, true);
        Indexer classIndexer = new Indexer();

        for ( URL classFile : liquibaseClasses ) {
            try {
                classIndexer.index(classFile.openStream());
            }
            catch ( IOException e ) {
                log.warn("Failed to index classes:", e); //$NON-NLS-1$
            }
        }

        this.classIndex = classIndexer.complete();
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.servicelocator.ServiceLocator#findClass(java.lang.Class)
     */
    @Override
    public Class findClass ( Class clazz ) {
        if ( log.isTraceEnabled() ) {
            log.trace("Trying to locate service for class " + clazz.getName()); //$NON-NLS-1$
        }

        @SuppressWarnings ( "unchecked" )
        ServiceReference<?> ref = this.bundleContext.getServiceReference(clazz);

        if ( ref != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Returning service class for class " + clazz.getName()); //$NON-NLS-1$
            }
            return this.bundleContext.getService(ref).getClass();
        }

        Collection<ClassInfo> candidates = findCandidates(clazz);

        for ( ClassInfo candidate : candidates ) {
            try {
                Class<?> c = this.loadCandidate(candidate);
                if ( c != null ) {
                    return c;
                }
            }
            catch (
                ClassNotFoundException |
                NoSuchMethodException |
                SecurityException e ) {
                log.warn("Failed to load class ", e); //$NON-NLS-1$
            }
        }

        throw new ServiceNotFoundException("No service found for class " + clazz.getName()); //$NON-NLS-1$
    }


    /**
     * @param clazz
     * @return
     */
    private Collection<ClassInfo> findCandidates ( Class<?> clazz ) {
        Collection<ClassInfo> candidates = this.classIndex.getAllKnownImplementors(DotName.createSimple(clazz.getName()));
        candidates.addAll(this.classIndex.getAllKnownSubclasses(DotName.createSimple(clazz.getName())));
        return candidates;
    }


    private Class<?> loadCandidate ( ClassInfo candidate ) throws ClassNotFoundException, NoSuchMethodException {
        if ( log.isTraceEnabled() ) {
            log.trace("Candidate: " + candidate.name()); //$NON-NLS-1$
        }
        if ( Modifier.isAbstract(candidate.flags()) || Modifier.isInterface(candidate.flags()) ) {
            return null;
        }

        Class<?> c = this.getClass().getClassLoader().loadClass(candidate.name().toString());

        if ( c.getAnnotation(LiquibaseService.class) != null && c.getAnnotation(LiquibaseService.class).skip() ) {
            return null;
        }

        c.getConstructor();
        return c;
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.servicelocator.ServiceLocator#findClasses(java.lang.Class)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public <T> Class<? extends T>[] findClasses ( Class<T> clazz ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Trying to locate services for class " + clazz.getName()); //$NON-NLS-1$
        }

        if ( this.classIndex == null ) {
            log.error("ServiceLocator not initialized"); //$NON-NLS-1$
            return new Class[] {};
        }

        Collection<ClassInfo> candidates = this.classIndex.getAllKnownImplementors(DotName.createSimple(clazz.getName()));
        candidates.addAll(this.classIndex.getAllKnownSubclasses(DotName.createSimple(clazz.getName())));

        List<Class<T>> classes = new ArrayList<>();
        Collection<ServiceReference<T>> refs;
        try {
            refs = this.bundleContext.getServiceReferences(clazz, null);
        }
        catch ( InvalidSyntaxException e1 ) {
            throw new ServiceNotFoundException("Failed to locate osgi service:", e1); //$NON-NLS-1$
        }

        for ( ServiceReference<T> ref : refs ) {
            T service = this.bundleContext.getService(ref);

            if ( service == null ) {
                log.warn("Service unavailable " + ref); //$NON-NLS-1$
            }
            else {
                classes.add((Class<T>) service.getClass());
            }
        }

        for ( ClassInfo candidate : candidates ) {
            try {
                Class<T> c = (Class<T>) this.loadCandidate(candidate);
                if ( c != null ) {
                    classes.add(c);
                }
            }
            catch ( NoSuchMethodException e ) {
                log.trace("Class has no default constructor, skip:", e); //$NON-NLS-1$
            }
            catch (
                ClassNotFoundException |
                SecurityException e ) {
                log.warn("Cannot instantiate class, skip:", e); //$NON-NLS-1$
            }
        }

        return classes.toArray(new Class[] {});
    }


    /**
     * {@inheritDoc}
     * 
     * @see liquibase.servicelocator.ServiceLocator#newInstance(java.lang.Class)
     */
    @Override
    public Object newInstance ( Class clazz ) {
        if ( log.isDebugEnabled() ) {
            log.debug("Trying to create instance for class " + clazz.getName()); //$NON-NLS-1$
        }

        @SuppressWarnings ( "unchecked" )
        ServiceReference<?> ref = this.bundleContext.getServiceReference(clazz);

        if ( ref != null ) {
            if ( log.isDebugEnabled() ) {
                log.debug("Returning service instance for class " + clazz.getName()); //$NON-NLS-1$
            }
            return this.bundleContext.getService(ref);
        }

        return super.newInstance(clazz);
    }
}
