/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.util.Set;

import javax.validation.Valid;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( FileshareContentConfigObjectTypeDescriptor.TYPE_NAME )
public interface FileshareContentConfig extends ConfigurationObject {

    /**
     * 
     * @return whether to use user supplied information (mime-type, filename) for determining mime type
     */
    Boolean getUseUserSuppliedTypeInfo ();


    /**
     * 
     * @return the mime type to use when none can be determined
     */
    String getFallbackMimeType ();


    /**
     * 
     * @return blacklisted mime types
     */
    Set<String> getBlacklistMimeTypes ();


    /**
     * 
     * @return whitelisted mime types
     */
    Set<String> getWhitelistMimeTypes ();


    /**
     * 
     * @return allow users to change mime types
     */
    Boolean getAllowMimeTypeChanges ();


    /**
     * 
     * @return content scanning config
     */
    @ReferencedObject
    @Valid
    FileshareContentScanConfig getScanConfig ();


    /**
     * 
     * @return search config
     */
    @ReferencedObject
    @Valid
    FileshareContentSearchConfig getSearchConfig ();


    /**
     * 
     * @return preview config
     */
    @ReferencedObject
    @Valid
    FileshareContentPreviewConfig getPreviewConfig ();

}
