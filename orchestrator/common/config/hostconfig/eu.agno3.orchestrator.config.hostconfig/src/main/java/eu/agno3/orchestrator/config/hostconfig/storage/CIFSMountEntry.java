/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import eu.agno3.orchestrator.config.model.realm.ObjectTypeName;


/**
 * @author mbechler
 *
 */
@ObjectTypeName ( "urn:agno3:objects:1.0:hostconfig:storage:mount:cifs" )
public interface CIFSMountEntry extends MountEntry {

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
