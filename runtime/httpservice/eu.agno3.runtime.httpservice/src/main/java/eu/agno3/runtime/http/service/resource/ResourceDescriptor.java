/**
 * © 2013 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 18.07.2013 by mbechler
 */
package eu.agno3.runtime.http.service.resource;


import java.io.Serializable;
import java.util.Set;

import org.osgi.framework.Bundle;


/**
 * @author mbechler
 * 
 */
public interface ResourceDescriptor extends Serializable {

    /**
     * @return the path under which this resource is registered
     */
    String getPath ();


    /**
     * 
     * @return the contexts under which this resource will be registered, or null if all
     */
    Set<String> getContexts ();


    /**
     * 
     * @return the registrations priority, if multiple matches are found a higher priority will win
     */
    int getPriority ();


    /**
     * 
     * @return the bundle which serves the resource
     */
    Bundle getBundle ();


    /**
     * 
     * @return the path inside the bundle from which the resources shall be served
     */
    String getResourceBase ();
}