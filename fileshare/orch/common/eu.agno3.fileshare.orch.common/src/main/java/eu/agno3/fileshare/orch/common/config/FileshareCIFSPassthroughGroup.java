/**
 * © 2016 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 20.02.2016 by mbechler
 */
package eu.agno3.fileshare.orch.common.config;


import eu.agno3.orchestrator.config.hostconfig.storage.CIFSAuthType;
import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:fileshare:passthroughGroup:cifs" )
public interface FileshareCIFSPassthroughGroup extends FilesharePassthroughGroup {

    /**
     * 
     * @return authentication method
     */
    CIFSAuthType getAuthType ();


    /**
     * 
     * @return login domain
     */
    String getDomain ();


    /**
     * 
     * @return login password
     */
    String getPassword ();


    /**
     * 
     * @return login username
     */
    String getUsername ();


    /**
     * 
     * @return server
     */
    String getUncPath ();


    /**
     * @return authentication realm
     */
    String getAuthRealm ();


    /**
     * @return authentication keytab
     */
    String getAuthKeytab ();


    /**
     * @return whether to enable smb signing
     */
    Boolean getEnableSigning ();


    /**
     * 
     * @return whether to disable SMB2 support
     */
    Boolean getDisableSMB2 ();


    /**
     * 
     * @return whether to allow SMB1 fallback
     */
    Boolean getAllowSMB1 ();

}
