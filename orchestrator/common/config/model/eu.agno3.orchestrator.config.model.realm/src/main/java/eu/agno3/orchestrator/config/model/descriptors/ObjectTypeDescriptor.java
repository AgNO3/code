/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 29.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;


/**
 * @author mbechler
 * @param <T>
 *
 */
public interface ObjectTypeDescriptor <T extends ConfigurationObject> {

    /**
     * @return the object type name
     */
    String getObjectTypeName ();


    /**
     * @return the object type class
     */
    @NonNull
    Class<@Nullable T> getObjectType ();


    /**
     * 
     * @return whether to hide this type from object type listings
     */
    boolean isHidden ();


    /**
     * 
     * @return the localization base
     */
    String getLocalizationBase ();


    /**
     * 
     * @return the parent type name
     */
    String getParentTypeName ();


    /**
     * @return whether this is a abstarct type
     */
    boolean isAbstract ();
}
