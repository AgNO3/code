/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 24.07.2015 by mbechler
 */
package eu.agno3.orchestrator.config.hostconfig.storage;


import java.net.URI;

import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( NFSMountEntry.class )
public interface NFSMountEntryMutable extends NFSMountEntry, MountEntryMutable {

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
     * @param securityType
     */
    void setSecurityType ( NFSSecurityType securityType );


    /**
     * 
     * @param nfsVersion
     */
    void setNfsVersion ( NFSVersion nfsVersion );


    /**
     * 
     * @param target
     */
    void setTarget ( URI target );

}
