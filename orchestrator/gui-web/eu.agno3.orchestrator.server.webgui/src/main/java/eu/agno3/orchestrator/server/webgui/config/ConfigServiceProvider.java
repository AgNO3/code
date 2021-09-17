/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.11.2014 by mbechler
 */
package eu.agno3.orchestrator.server.webgui.config;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;

import eu.agno3.orchestrator.config.model.descriptors.ImageTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ObjectTypeRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ResourceLibraryRegistry;
import eu.agno3.orchestrator.config.model.descriptors.ServiceTypeRegistry;


/**
 * @author mbechler
 *
 */
@ApplicationScoped
public class ConfigServiceProvider {

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    protected ServiceTypeRegistry serviceTypeRegistry;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    protected ObjectTypeRegistry objectTypeRegistry;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    protected ImageTypeRegistry imageTypeRegistry;

    @Inject
    @OsgiService ( dynamic = true, timeout = 500 )
    private ResourceLibraryRegistry resourceLibraryRegistry;


    /**
     * @return the serviceTypeRegistry
     */
    public ServiceTypeRegistry getServiceTypeRegistry () {
        return this.serviceTypeRegistry;
    }


    /**
     * @return the objectTypeRegistry
     */
    public ObjectTypeRegistry getObjectTypeRegistry () {
        return this.objectTypeRegistry;
    }


    /**
     * @return the imageTypeRegitry
     */
    public ImageTypeRegistry getImageTypeRegistry () {
        return this.imageTypeRegistry;
    }


    public ResourceLibraryRegistry getResourceLibraryRegistry () {
        return this.resourceLibraryRegistry;
    }
}
