/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.12.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


import java.util.Set;


/**
 * @author mbechler
 *
 */
public interface ImageTypeDescriptor {

    /**
     * @return the image type id
     */
    String getId ();


    /**
     * 
     * @return the localization base
     */
    String getLocalizationBase ();


    /**
     * @return the services that need to be configured
     */
    Set<String> getForcedServiceTypes ();


    /**
     * @return the services (apart from forced services) that may be configured
     */
    Set<String> getApplicableServiceTypes ();
}
