/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 28.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.util.Set;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:runtime" )
public interface RuntimeConfiguration extends ConfigurationObject {

    /**
     * @return the heap memory limit
     */
    Long getMemoryLimit ();


    /**
     * @return whether memory limit is automatically set
     */
    Boolean getAutoMemoryLimit ();


    /**
     * @return packages to enable debugging for
     */
    Set<String> getDebugPackages ();


    /**
     * @return packages to eanble trace level debugging for
     */
    Set<String> getTracePackages ();

}
