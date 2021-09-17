/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 22.01.2014 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.util.Set;


/**
 * @author mbechler
 * 
 */
public abstract class AbstractResourceDescriptor implements ResourceDescriptor {

    /**
     * 
     */
    private static final long serialVersionUID = 502326952685923788L;
    private String path;
    private Set<String> contexts;
    private int priority;
    private String resourceBase;


    /**
     * @param path
     * @param contexts
     * @param priority
     * @param resourceBase
     * 
     */
    public AbstractResourceDescriptor ( String path, Set<String> contexts, int priority, String resourceBase ) {
        super();
        this.path = path;
        this.contexts = contexts;
        this.priority = priority;
        this.resourceBase = resourceBase;
    }


    /**
     * 
     */
    AbstractResourceDescriptor () {}


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getPath()
     */
    @Override
    public String getPath () {
        return this.path;
    }


    /**
     * @param path
     *            the path to set
     */
    final void setPath ( String path ) {
        this.path = path;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getContexts()
     */
    @Override
    public Set<String> getContexts () {
        return this.contexts;
    }


    /**
     * @param contexts
     *            the contexts to set
     */
    final void setContexts ( Set<String> contexts ) {
        this.contexts = contexts;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getPriority()
     */
    @Override
    public int getPriority () {
        return this.priority;
    }


    /**
     * @param priority
     *            the priority to set
     */
    final void setPriority ( int priority ) {
        this.priority = priority;
    }


    /**
     * {@inheritDoc}
     * 
     * @see eu.agno3.runtime.http.service.resource.ResourceDescriptor#getResourceBase()
     */
    @Override
    public String getResourceBase () {
        return this.resourceBase;
    }


    /**
     * @param resourceBase
     *            the resourceBase to set
     */
    final void setResourceBase ( String resourceBase ) {
        this.resourceBase = resourceBase;
    }

}