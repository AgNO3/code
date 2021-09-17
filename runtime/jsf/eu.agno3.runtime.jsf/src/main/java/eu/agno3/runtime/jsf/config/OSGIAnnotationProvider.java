/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.09.2013 by mbechler
 */
package eu.agno3.runtime.jsf.config;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.component.FacesComponent;
import javax.faces.component.behavior.FacesBehavior;
import javax.faces.context.ExternalContext;
import javax.faces.convert.FacesConverter;
import javax.faces.event.NamedEvent;
import javax.faces.render.FacesBehaviorRenderer;
import javax.faces.render.FacesRenderer;
import javax.faces.validator.FacesValidator;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.apache.myfaces.spi.AnnotationProvider;
import org.apache.xbean.finder.BundleAssignableClassFinder;
import org.apache.xbean.osgi.bundle.util.BundleClassFinder;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.PackageAdmin;


/**
 * @author mbechler
 * 
 */
@SuppressWarnings ( "deprecation" )
public class OSGIAnnotationProvider extends AnnotationProvider {

    private static final Logger log = Logger.getLogger(OSGIAnnotationProvider.class);
    private static Set<Class<? extends Annotation>> byteCodeAnnotationsNames;

    static {
        Set<Class<? extends Annotation>> bcan = new HashSet<>(10, 1f);
        bcan.add(FacesComponent.class);
        bcan.add(FacesBehavior.class);
        bcan.add(FacesConverter.class);
        bcan.add(FacesValidator.class);
        bcan.add(FacesRenderer.class);
        bcan.add(ManagedBean.class);
        bcan.add(NamedEvent.class);
        bcan.add(FacesBehaviorRenderer.class);
        byteCodeAnnotationsNames = Collections.unmodifiableSet(bcan);
    }


    /**
     * {@inheritDoc}
     * 
     * @see org.apache.myfaces.spi.AnnotationProvider#getAnnotatedClasses(javax.faces.context.ExternalContext)
     */
    @SuppressWarnings ( "unchecked" )
    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses ( ExternalContext ctx ) {
        long start = System.currentTimeMillis();
        log.debug("Returning annotated bean classes"); //$NON-NLS-1$

        Bundle b = (Bundle) ( (ServletContext) ctx.getContext() ).getAttribute("context.bundle"); //$NON-NLS-1$

        ServiceReference<PackageAdmin> reference = (ServiceReference<PackageAdmin>) b.getBundleContext().getServiceReference(
            PackageAdmin.class.getName());
        PackageAdmin packageAdmin = b.getBundleContext().getService(reference);

        BundleClassFinder bundleClassFinder = new BundleAssignableClassFinder(packageAdmin, b, new Class<?>[] {
            Object.class
        }, new BundleClasspathFilter());

        Map<Class<? extends Annotation>, Set<Class<?>>> classes = findClasses(bundleClassFinder);

        if ( log.isDebugEnabled() ) {
            log.debug(String.format("Annotated class scanning took %.2f", ( System.currentTimeMillis() - start ) / 1000.f)); //$NON-NLS-1$
        }
        return classes;
    }


    /**
     * @param bundleClassFinder
     * @return
     */
    private static Map<Class<? extends Annotation>, Set<Class<?>>> findClasses ( BundleClassFinder bundleClassFinder ) {
        Map<Class<? extends Annotation>, Set<Class<?>>> result = new HashMap<>();

        for ( Class<? extends Annotation> annotType : byteCodeAnnotationsNames ) {
            result.put(annotType, new HashSet<Class<?>>());
        }

        Set<String> acceptedClassNames = bundleClassFinder.find();
        List<Class<?>> acceptedClasses = bundleClassFinder.loadClasses(acceptedClassNames);
        Set<Class<? extends Annotation>> matches = new HashSet<>();
        for ( Class<?> clazz : acceptedClasses ) {
            if ( log.isTraceEnabled() ) {
                log.trace("Found class " + clazz.getName()); //$NON-NLS-1$
            }
            matches.clear();

            for ( Annotation annot : clazz.getAnnotations() ) {
                if ( log.isTraceEnabled() ) {
                    log.trace("Found annot " + annot.annotationType().getName()); //$NON-NLS-1$
                }

                if ( byteCodeAnnotationsNames.contains(annot.annotationType()) ) {
                    matches.add(annot.annotationType());
                }
            }

            for ( Class<? extends Annotation> type : matches ) {
                if ( log.isDebugEnabled() ) {
                    log.debug("Adding " + clazz.getName() + " as " + type.getName()); //$NON-NLS-1$//$NON-NLS-2$
                }
                result.get(type).add(clazz);
            }
        }
        return result;
    }


    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public Set<URL> getBaseUrls () throws IOException {
        return Collections.EMPTY_SET;
    }

}
