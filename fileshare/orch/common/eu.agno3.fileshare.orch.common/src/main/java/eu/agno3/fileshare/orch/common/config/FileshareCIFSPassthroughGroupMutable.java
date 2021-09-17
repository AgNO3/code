/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( FileshareCIFSPassthroughGroup.class )
public interface FileshareCIFSPassthroughGroupMutable extends FilesharePassthroughGroup {

    /**
     * 
     * @param enableSigning
     */
    void setEnableSigning ( Boolean enableSigning );


    /**
     * 
     * @param authKeytab
     */
    void setAuthKeytab ( String authKeytab );


    /**
     * 
     * @param authRealm
     */
    void setAuthRealm ( String authRealm );


    /**
     * 
     * @param authType
     */
    void setAuthType ( CIFSAuthType authType );


    /**
     * 
     * @param domain
     */
    void setDomain ( String domain );


    /**
     * 
     * @param password
     */
    void setPassword ( String password );


    /**
     * 
     * @param username
     */
    void setUsername ( String username );


    /**
     * 
     * @param path
     */
    void setUncPath ( String path );


    /**
     * 
     * @param disableSMB2
     */
    void setDisableSMB2 ( Boolean disableSMB2 );


    /**
     * 
     * @param allowSMB1
     */
    void setAllowSMB1 ( Boolean allowSMB1 );
}
