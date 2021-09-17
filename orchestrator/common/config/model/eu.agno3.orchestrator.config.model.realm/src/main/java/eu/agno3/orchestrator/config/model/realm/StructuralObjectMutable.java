/**
 * © 2014 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 12.06.2014 by mbechler
 */
package eu.agno3.orchestrator.config.model.realm;


import java.util.UUID;


/**
 * @author mbechler
 * 
 */
public interface StructuralObjectMutable extends StructuralObject {

    /**
     * @param id
     *            the id to set
     */
    void setId ( UUID id );


    /**
     * @param version
     *            the version to set
     */
    void setVersion ( Long version );


    /**
     * @param displayName
     *            the displayName to set
     */
    void setDisplayName ( String displayName );

}