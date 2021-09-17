/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.util.Arrays;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;


/**
 * @author mbechler
 * 
 */
public class AnnotationResource extends AbstractResourceDescriptor {

    /**
     * 
     */
    private static final long serialVersionUID = -4707928794556655846L;

    private static final Logger log = Logger.getLogger(AnnotationResource.class);


    /**
     * Registeres a resource using annotation config
     */
    public AnnotationResource () {
        super();
        if ( !this.getClass().isAnnotationPresent(ResourceConfig.class) ) {
            log.error("Annotation resource without a ResourceConfig annotation"); //$NON-NLS-1$
            return;
        }
        ResourceConfig config = this.getClass().getAnnotation(ResourceConfig.class);

        this.setPath(config.paths());
        if ( config.contexts().length > 0 ) {
            this.setContexts(new HashSet<>(Arrays.asList(config.contexts())));
        }
        this.setPriority(config.priority());
        this.setResourceBase(config.resourceBase());
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getBundle()
     */
    @Override
    public Bundle getBundle () {
        return FrameworkUtil.getBundle(this.getClass());
    }

}
