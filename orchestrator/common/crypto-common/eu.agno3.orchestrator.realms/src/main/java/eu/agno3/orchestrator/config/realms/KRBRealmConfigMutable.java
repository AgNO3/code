/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 08.04.2015 by mbechler
 */
package eu.agno3.orchestrator.config.realms;


import java.util.List;

import eu.agno3.orchestrator.realms.RealmType;
import eu.agno3.runtime.xml.binding.MapAs;


/**
 * @author mbechler
 *
 */
@MapAs ( KRBRealmConfig.class )
public interface KRBRealmConfigMutable extends KRBRealmConfig, RealmConfigMutable {

    /**
     * @param realmType
     *            the realmType to set
     */
    @Override
    void setRealmType ( RealmType realmType );


    /**
     * @param adminServer
     *            the adminServer to set
     */
    void setAdminServer ( String adminServer );


    /**
     * @param kpasswdServer
     *            the kpasswdServer to set
     */
    void setKpasswdServer ( String kpasswdServer );


    /**
     * @param kdcs
     *            the kdcs to set
     */
    void setKdcs ( List<String> kdcs );

}