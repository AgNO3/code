/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 13.12.2014 by mbechler
 */
package eu.agno3.orchestrator.types;


/**
 * 
 * Interface for database entities which are deduplicated in a global context
 * 
 * Implementors must:
 * - use the derived id property as \@Id property
 * - ensure that the derived id is sufficiently collision free
 * - ensure that the derived id is updated when properties change
 * - implement hashCode and equals based on it
 * - ensure that the object is not modified after persisting it
 * 
 * Version should be persisted, if it changes the deduplicated object will be replaced with a new version upon the next
 * save.
 * 
 * @author mbechler
 *
 */
public interface DeduplicatedGlobal {

    /**
     * 
     * @return an id that is unique but reproducable for this object
     */
    String getDerivedId ();


    /**
     * 
     * @return format version
     */
    Integer getVersion ();


    /**
     * @param o
     */
    void replace ( DeduplicatedGlobal o );
}
