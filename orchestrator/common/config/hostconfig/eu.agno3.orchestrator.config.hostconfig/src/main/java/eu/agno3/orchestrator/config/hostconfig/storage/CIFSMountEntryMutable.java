/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( CIFSMountEntry.class )
public interface CIFSMountEntryMutable extends CIFSMountEntry, MountEntryMutable {

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
     * @param authRealm
     */
    void setAuthRealm ( String authRealm );


    /**
     * @param authKeytab
     */
    void setAuthKeytab ( String authKeytab );


    /**
     * @param enableSigning
     */
    void setEnableSigning ( Boolean enableSigning );


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
