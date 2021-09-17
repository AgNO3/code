/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 02.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.web;


import java.net.URI;
import java.util.List;

import javax.validation.Valid;

import org.joda.time.Duration;

import eu.agno3.orchestrator.config.model.base.config.ReferencedObject;
import eu.agno3.orchestrator.config.model.realm.ConfigurationObject;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:web:icap" )
public interface ICAPConfiguration extends ConfigurationObject {

    /**
     * 
     * @return the ssl client mode
     */
    SSLClientMode getSslClientMode ();


    /**
     * 
     * @return the ssl client configuration
     */
    @ReferencedObject
    @Valid
    SSLClientConfiguration getSslClientConfiguration ();


    /**
     * 
     * @return the socket timeout
     */
    Duration getSocketTimeout ();


    /**
     * 
     * @return override request URI in ICAP requests
     */
    String getOverrideRequestURI ();


    /**
     * 
     * @return the icap servers
     */
    List<URI> getServers ();


    /**
     * @return whether to send an icaps:// URI as request url, if ssl is enabled
     */
    Boolean getSendICAPSInRequestUri ();
}
