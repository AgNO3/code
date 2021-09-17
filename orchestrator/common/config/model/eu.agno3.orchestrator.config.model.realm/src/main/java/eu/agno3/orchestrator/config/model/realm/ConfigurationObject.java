/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 11.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import org.eclipse.jdt.annotation.NonNull;

import eu.agno3.orchestrator.config.model.base.BaseObject;


/**
 * @author mbechler
 * 
 */
public interface ConfigurationObject extends BaseObject {

    /**
     * @return the configuration object type
     */
    @NonNull
    Class<? extends ConfigurationObject> getType ();


    /**
     * @return the effective display name for this object
     */
    String getDisplayName ();


    /**
     * @return the object name, null if this is an anonymous instance
     */
    String getName ();


    /**
     * @return the inherits
     */
    ConfigurationObject getInherits ();


    /**
     * @return the global revision for which this object is valid, null if unknown
     */
    Long getRevision ();

}