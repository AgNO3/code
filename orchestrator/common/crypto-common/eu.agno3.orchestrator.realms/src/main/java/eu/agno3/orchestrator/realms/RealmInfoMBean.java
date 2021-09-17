/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 10.04.2015 by mbechler
 */
package eu.agno3.orchestrator.realms;


import java.util.Collection;


/**
 * @author mbechler
 *
 */
public interface RealmInfoMBean {

    /**
     * @return the realmName
     */
    String getRealmName ();


    /**
     * @return the type
     */
    RealmType getType ();


    /**
     * @return the keytabs
     */
    Collection<KeytabInfo> getKeytabs ();

}