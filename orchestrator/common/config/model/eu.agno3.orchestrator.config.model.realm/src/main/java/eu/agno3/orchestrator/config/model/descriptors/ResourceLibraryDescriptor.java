/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 25.06.2015 by mbechler
 */
package eu.agno3.orchestrator.config.model.descriptors;


/**
 * @author mbechler
 *
 */
public interface ResourceLibraryDescriptor {

    /**
     * 
     * @return the library type
     */
    String getLibraryType ();


    /**
     * 
     * @return the localization base
     */
    String getLocalizationBase ();


    /**
     * @param name
     * @return whether an implicit root is providing defaults
     */
    boolean haveDefaultsFor ( String name );


    /**
     * @return the type of editor to use
     */
    String getEditorType ();

}
