/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.web.ICAPConfiguration;
import eu.agno3.runtime.validation.ValidConditional;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:content:scan" )
public interface FileshareContentScanConfig extends ConfigurationObject {

    /**
     * 
     * @return icap server configuration
     */
    @ReferencedObject
    @ValidConditional ( when = "#{enableICAP}" )
    ICAPConfiguration getIcapConfig ();


    /**
     * 
     * @return enable icap scanning
     */
    Boolean getEnableICAP ();

}
