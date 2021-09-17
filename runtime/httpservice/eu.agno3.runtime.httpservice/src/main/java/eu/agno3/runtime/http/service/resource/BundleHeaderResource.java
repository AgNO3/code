/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.util.Set;

import org.osgi.framework.Bundle;


/**
 * Represent a resource registration based on a bundle header
 * 
 * The bundle header used is "WWW-Resource" which is a list of registration separated by ";". Each registration has the
 * following format:
 * 
 * {@code
 * <url-pattern>[@<priority>][:<base-directory>[:<comma separated list of connectors to bind to>]]
 * }
 * 
 * @author mbechler
 * 
 */
public class BundleHeaderResource extends AbstractResourceDescriptor {

    /**
     * 
     */
    private static final long serialVersionUID = -657669220612672296L;
    private Bundle bundle;


    /**
     * @param path
     * @param contexts
     * @param priority
     * @param resourceBase
     * @param bundle
     */
    public BundleHeaderResource ( String path, Set<String> contexts, int priority, String resourceBase, Bundle bundle ) {
        super(path, contexts, priority, resourceBase);
        this.bundle = bundle;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getBundle()
     */
    @Override
    public Bundle getBundle () {
        return this.bundle;
    }

}
