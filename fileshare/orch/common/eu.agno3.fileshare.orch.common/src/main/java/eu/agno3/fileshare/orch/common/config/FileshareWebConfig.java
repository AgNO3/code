/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 30.06.2015 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import java.net.URI;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;
import eu.agno3.orchestrator.config.model.validation.Instance;
import eu.agno3.orchestrator.config.model.validation.Materialized;
import eu.agno3.orchestrator.config.web.WebEndpointConfig;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:web" )
public interface FileshareWebConfig extends ConfigurationObject {

    /**
     * 
     * @return the intent timeout
     */
    @NotNull ( groups = {
        Materialized.class
    } )
    Duration getIntentTimeout ();


    /**
     * 
     * @return the URI to use when generating links and no client supplied information is available
     */
    URI getOverrideBaseURI ();


    /**
     * 
     * @return whether to enable webdav support
     */
    Boolean getEnableWebDAV ();


    /**
     * 
     * @return whether to allow webdav clients to override the file modification time
     */
    Boolean getWebDAVAllowSetModificationTime ();


    /**
     * 
     * @return web endpoint configuration
     */
    @NotNull ( groups = {
        Instance.class, Materialized.class
    } )
    @ReferencedObject
    @Valid
    WebEndpointConfig getWebEndpointConfig ();


    /**
     * @return the theme library to use
     */
    String getThemeLibrary ();


    /**
     * 
     * @return the duration after which incomplete uploads will be removed for non user accounts
     */
    Duration getSessionIncompleteExpireDuration ();


    /**
     * 
     * @return whether per user storage upload temporary storage is constrained
     */
    Boolean getPerSessionIncompleteSizeLimitEnabled ();


    /**
     * 
     * @return the maximum total size (bytes) of stored incomplete uploads for non user accounts
     */
    Long getPerSessionIncompleteSizeLimit ();


    /**
     * 
     * @return the duration after which incomplete uploads will be removed for user accounts
     */
    Duration getUserIncompleteExpireDuration ();


    /**
     * 
     * @return whether per user storage upload temporary storage is constrained
     */
    Boolean getPerUserIncompleteSizeLimitEnabled ();


    /**
     * 
     * @return the maximum total size (bytes) of stored incomplete uploads for user accounts
     */
    Long getPerUserIncompleteSizeLimit ();


    /**
     * 
     * @return default upload chunk size to use
     */
    Long getDefaultUploadChunkSize ();


    /**
     * 
     * @return maximum upload chunk size to use
     */
    Long getMaximumUploadChunkSize ();


    /**
     * 
     * @return optimal upload chunk count to use
     */
    Integer getOptimalUploadChunkCount ();

}
