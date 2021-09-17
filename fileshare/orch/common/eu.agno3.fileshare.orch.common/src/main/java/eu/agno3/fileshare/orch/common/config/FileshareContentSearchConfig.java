/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:content:search" )
public interface FileshareContentSearchConfig extends ConfigurationObject {

    /**
     * 
     * @return whether to allow searching
     */
    Boolean getSearchDisabled ();


    /**
     * 
     * @return whether to allow paging
     */
    Boolean getSearchAllowPaging ();


    /**
     * 
     * @return search result page size (maximum number of results if paging is disabled)
     */
    Integer getSearchPageSize ();

}
