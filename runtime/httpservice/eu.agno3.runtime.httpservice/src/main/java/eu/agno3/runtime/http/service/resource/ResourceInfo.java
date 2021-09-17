/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 19.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.util.Collection;


/**
 * @author mbechler
 * 
 */
public interface ResourceInfo {

    /**
     * @param res
     * @return whether the resource is registered
     */
    boolean hasResource ( ResourceDescriptor res );


    /**
     * 
     * @return registered resources
     */
    Collection<ResourceDescriptor> getResource ();


    /**
     * @return the context this resource servlet handles
     */
    String getContextName ();
}
