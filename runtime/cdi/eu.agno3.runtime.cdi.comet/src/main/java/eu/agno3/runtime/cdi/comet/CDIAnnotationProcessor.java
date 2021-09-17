/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 03.12.2014 by mbechler
 */
package eu.agno3.runtime.cdi.comet;


import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.webbeans.spi.ScannerService;
import org.atmosphere.config.AtmosphereAnnotation;
import org.atmosphere.cpr.AnnotationHandler;
import org.atmosphere.cpr.AnnotationProcessor;
import org.atmosphere.cpr.AtmosphereConfig;
import org.atmosphere.cpr.DefaultAnnotationProcessor;

import eu.agno3.runtime.cdi.bootstrap.OsgiMetaDataScannerService;


/**
 * @author mbechler
 *
 */
public class CDIAnnotationProcessor extends DefaultAnnotationProcessor {

    private static final Logger log = Logger.getLogger(CDIAnnotationProcessor.class);

    private AnnotationHandler handler;
    private AtmosphereConfig config;
    private ScannerService scannerService;


    /**
     * 
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.DefaultAnnotationProcessor#configure(org.atmosphere.cpr.AtmosphereConfig)
     */
    @Override
    public void configure ( AtmosphereConfig cfg ) {
        this.scannerService = new OsgiMetaDataScannerService();
        this.scannerService.init(cfg.getServletContext());
        this.config = cfg;
        this.handler = new AnnotationHandler();
    }


    /**
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.DefaultAnnotationProcessor#scanAll()
     */
    @Override
    public AnnotationProcessor scanAll () throws IOException {
        log.debug("Scanning classes"); //$NON-NLS-1$
        Map<Class<? extends Annotation>, Set<Class<?>>> annotations = new HashMap<>();

        try {
            this.scannerService.scan();
            for ( Class<?> clz : this.scannerService.getBeanClasses() ) {
                for ( Annotation ann : clz.getAnnotations() ) {
                    if ( !annotations.containsKey(ann.annotationType()) ) {
                        annotations.put(ann.annotationType(), new HashSet<Class<?>>());
                    }
                    annotations.get(ann.annotationType()).add(clz);
                }
            }

            Set<Class<?>> atmosphereAnnotations = annotations.get(AtmosphereAnnotation.class);

            if ( atmosphereAnnotations != null ) {
                for ( Class<?> atmosphereAnnotationProcCls : atmosphereAnnotations ) {
                    if ( log.isDebugEnabled() ) {
                        log.debug("Found annotation processor " + atmosphereAnnotationProcCls); //$NON-NLS-1$
                    }
                    this.handler.handleProcessor(atmosphereAnnotationProcCls);
                }
            }

            for ( Class<?> cls : this.handler.handledClass() ) {

                if ( !Annotation.class.isAssignableFrom(cls) ) {
                    continue;
                }

                @SuppressWarnings ( "unchecked" )
                Class<? extends Annotation> annotClass = (Class<? extends Annotation>) cls;

                Set<Class<?>> annotatedClasses = annotations.get(annotClass);

                if ( annotatedClasses != null ) {
                    for ( Class<?> clz : annotatedClasses ) {
                        if ( log.isDebugEnabled() ) {
                            log.debug(String.format("Found class %s with annotation %s", clz.getName(), annotClass.getName())); //$NON-NLS-1$
                        }
                        this.handler.handleAnnotation(this.config.framework(), annotClass, clz);
                    }
                }
            }
        }
        finally {
            this.scannerService.release();
        }

        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.DefaultAnnotationProcessor#scan(java.io.File)
     */
    @Override
    public AnnotationProcessor scan ( File rootDir ) throws IOException {
        return this;
    }


    /**
     * {@inheritDoc}
     *
     * @see org.atmosphere.cpr.DefaultAnnotationProcessor#scan(java.lang.String)
     */
    @Override
    public AnnotationProcessor scan ( String packageName ) throws IOException {
        return this;
    }
}
