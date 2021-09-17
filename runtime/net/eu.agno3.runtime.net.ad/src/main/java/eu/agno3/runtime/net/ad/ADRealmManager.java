/**
 * © 2015 AgNO3 Gmbh & Co. KG
 * All right reserved.
 * 
 * Created: 01.04.2015 by mbechler
 */
package eu.agno3.runtime.net.ad;


import eu.agno3.runtime.net.ad.internal.RealmManagerADRealmConfig;
import eu.agno3.runtime.net.krb5.KerberosException;
import eu.agno3.runtime.net.krb5.KrbRealmManager;


/**
 * @author mbechler
 *
 */
public interface ADRealmManager extends KrbRealmManager {

    /**
     * 
     * {@inheritDoc}
     *
     * @see eu.agno3.runtime.net.krb5.KrbRealmManager#getRealmInstance(java.lang.String)
     */
    @Override
    ADRealm getRealmInstance ( String domainName ) throws KerberosException;


    /**
     * @param domainName
     * @return realm configuration
     * @throws KerberosException
     */
    @Override
    RealmManagerADRealmConfig getRealmConfig ( String domainName ) throws KerberosException;

}
